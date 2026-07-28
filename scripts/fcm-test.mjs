#!/usr/bin/env node

import { createSign } from 'node:crypto';
import { readFile } from 'node:fs/promises';

const TOKEN_URI = 'https://oauth2.googleapis.com/token';
const FCM_SCOPE = 'https://www.googleapis.com/auth/firebase.messaging';

const env = process.env;

function base64Url(input) {
  return Buffer.from(input)
    .toString('base64')
    .replaceAll('+', '-')
    .replaceAll('/', '_')
    .replaceAll('=', '');
}

function boolFromEnv(value, defaultValue) {
  if (value == null || value === '') {
    return defaultValue;
  }

  return !['false', '0', 'no', 'n'].includes(value.toLowerCase());
}

function usage() {
  console.log(`
Usage:
  FIREBASE_SERVICE_ACCOUNT_PATH=/path/firebase-adminsdk.json \\
  FCM_TOKEN=ios-device-fcm-token \\
  node scripts/fcm-test.mjs

  FIREBASE_SERVICE_ACCOUNT_PATH=/path/firebase-adminsdk.json \\
  FCM_TOPIC=fcm-test \\
  node scripts/fcm-test.mjs

Options via env:
  DRY_RUN=false              Send a real push. Default: true
  FCM_TITLE="청바지 테스트"     Notification title
  FCM_BODY="FCM 연결 확인"     Notification body
  FCM_PROJECT_ID=project-id  Override project_id in service account JSON
  APNS_TOPIC=bundle.id       Optional iOS bundle id for apns-topic header
`);
}

async function createAccessToken(serviceAccount) {
  const now = Math.floor(Date.now() / 1000);
  const header = {
    alg: 'RS256',
    typ: 'JWT',
  };
  const claim = {
    iss: serviceAccount.client_email,
    scope: FCM_SCOPE,
    aud: serviceAccount.token_uri || TOKEN_URI,
    iat: now,
    exp: now + 3600,
  };

  const unsignedJwt = `${base64Url(JSON.stringify(header))}.${base64Url(JSON.stringify(claim))}`;
  const signature = createSign('RSA-SHA256')
    .update(unsignedJwt)
    .sign(serviceAccount.private_key);
  const assertion = `${unsignedJwt}.${base64Url(signature)}`;

  const res = await fetch(serviceAccount.token_uri || TOKEN_URI, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion,
    }),
  });

  const body = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(`OAuth token request failed (${res.status}): ${JSON.stringify(body)}`);
  }

  return body.access_token;
}

async function main() {
  if (env.HELP === 'true' || process.argv.includes('--help')) {
    usage();
    return;
  }

  const serviceAccountPath = env.FIREBASE_SERVICE_ACCOUNT_PATH;
  const fcmToken = env.FCM_TOKEN;
  const fcmTopic = env.FCM_TOPIC;

  if (!serviceAccountPath || (!fcmToken && !fcmTopic)) {
    usage();
    throw new Error('FIREBASE_SERVICE_ACCOUNT_PATH and one of FCM_TOKEN or FCM_TOPIC are required.');
  }

  const serviceAccount = JSON.parse(await readFile(serviceAccountPath, 'utf8'));
  const projectId = env.FCM_PROJECT_ID || serviceAccount.project_id;
  const dryRun = boolFromEnv(env.DRY_RUN, true);

  if (!projectId) {
    throw new Error('project_id is missing. Set FCM_PROJECT_ID or use a valid service account JSON.');
  }

  const accessToken = await createAccessToken(serviceAccount);
  const apnsHeaders = {
    'apns-priority': '10',
  };

  if (env.APNS_TOPIC) {
    apnsHeaders['apns-topic'] = env.APNS_TOPIC;
  }

  const payload = {
    validate_only: dryRun,
    message: {
      notification: {
        title: env.FCM_TITLE || '청바지 테스트',
        body: env.FCM_BODY || 'FCM 연결 확인',
      },
      data: {
        type: 'FCM_TEST',
        dryRun: String(dryRun),
      },
      apns: {
        headers: apnsHeaders,
        payload: {
          aps: {
            sound: 'default',
          },
        },
      },
    },
  };

  if (fcmToken) {
    payload.message.token = fcmToken;
  } else {
    payload.message.topic = fcmTopic;
  }

  const res = await fetch(`https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  const body = await res.json().catch(() => ({}));

  if (!res.ok) {
    console.error(JSON.stringify(body, null, 2));
    throw new Error(`FCM request failed (${res.status}).`);
  }

  console.log(JSON.stringify({
    ok: true,
    dryRun,
    projectId,
    target: fcmToken ? 'token' : `topic:${fcmTopic}`,
    response: body,
  }, null, 2));
}

main().catch((error) => {
  console.error(`\n${error.message}`);
  process.exit(1);
});
