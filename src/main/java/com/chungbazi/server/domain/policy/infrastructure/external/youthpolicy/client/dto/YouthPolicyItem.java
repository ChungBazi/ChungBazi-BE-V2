package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouthPolicyItem(
        // 정책 번호
        String plcyNo,
        // 정책명
        String plcyNm,
        // 정책 키워드명
        String plcyKywdNm,
        // 정책 설명
        String plcyExplnCn,
        // 정책 대분류명
        String lclsfNm,
        // 정책 중분류명
        String mclsfNm,
        // 정책 지원 내용
        String plcySprtCn,
        // 정책 제공 방법 코드
        String plcyPvsnMthdCd,
        // 주관 기관명
        String sprvsnInstCdNm,
        // 운영 기관명
        String operInstCdNm,
        // 신청 기간 구분 코드
        String aplyPrdSeCd,
        // 사업 기간 구분 코드
        String bizPrdSeCd,
        // 사업 시작일
        String bizPrdBgngYmd,
        // 사업 종료일
        String bizPrdEndYmd,
        // 기타 사업 기간 내용
        String bizPrdEtcCn,
        // 정책 신청 방법
        String plcyAplyMthdCn,
        // 심사 방법
        String srngMthdCn,
        // 신청 URL
        String aplyUrlAddr,
        // 제출 서류
        String sbmsnDcmntCn,
        // 기타 유의사항
        String etcMttrCn,
        // 참고 URL 1
        String refUrlAddr1,
        // 참고 URL 2
        String refUrlAddr2,
        // 지원 대상 최소 연령
        String sprtTrgtMinAge,
        // 지원 대상 최대 연령
        String sprtTrgtMaxAge,
        // 지원 대상 연령 제한 여부
        String sprtTrgtAgeLmtYn,
        // 혼인 상태 코드
        String mrgSttsCd,
        // 소득 조건 구분 코드
        String earnCndSeCd,
        // 최소 소득 금액
        String earnMinAmt,
        // 최대 소득 금액
        String earnMaxAmt,
        // 기타 소득 조건
        String earnEtcCn,
        // 추가 신청 자격 조건
        String addAplyQlfcCndCn,
        // 참여 제한 대상
        String ptcpPrpTrgtCn,
        // 조회 수
        String inqCnt,
        // 정책 지역 코드
        String zipCd,
        // 전공 요건 코드
        String plcyMajorCd,
        // 취업 상태 코드
        String jobCd,
        // 학력 요건 코드
        String schoolCd,
        // 신청 기간
        String aplyYmd,
        // 최초 등록 일시
        String frstRegDt,
        // 최종 수정 일시
        String lastMdfcnDt,
        // 정책 사업 코드
        String sbizCd
) {
}
