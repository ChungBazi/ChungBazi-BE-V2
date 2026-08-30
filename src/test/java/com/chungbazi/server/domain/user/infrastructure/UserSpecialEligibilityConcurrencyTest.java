package com.chungbazi.server.domain.user.infrastructure;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserSpecialEligibility;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import com.chungbazi.server.domain.user.domain.type.SpecialEligibilityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "firebase.enabled=false")
@ActiveProfiles("test")
class UserSpecialEligibilityConcurrencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSpecialEligibilityRepository eligibilityRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
        eligibilityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void updatesForSameUserAreSerialized() throws Exception {
        User user = userRepository.save(User.create(
                "concurrency-provider",
                SocialType.KAKAO,
                "concurrency@example.com",
                "test-user",
                null
        ));
        CountDownLatch firstUpdated = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);

        Future<?> first = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
            replace(lock(user.getId()), Set.of(SpecialEligibilityType.WOMAN));
            firstUpdated.countDown();
            await(releaseFirst);
        }));
        assertThat(firstUpdated.await(3, TimeUnit.SECONDS)).isTrue();

        Future<?> second = executor.submit(() -> transactionTemplate.executeWithoutResult(status ->
                replace(lock(user.getId()), Set.of(SpecialEligibilityType.FARMER))
        ));

        assertThatThrownBy(() -> second.get(300, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);

        releaseFirst.countDown();
        first.get(3, TimeUnit.SECONDS);
        second.get(3, TimeUnit.SECONDS);

        Set<SpecialEligibilityType> saved = transactionTemplate.execute(status -> {
            User savedUser = userRepository.findById(user.getId()).orElseThrow();
            return eligibilityRepository.findAllByUser(savedUser).stream()
                    .map(UserSpecialEligibility::getEligibilityType)
                    .collect(Collectors.toSet());
        });

        assertThat(saved).containsExactly(SpecialEligibilityType.FARMER);
    }

    private User lock(Long userId) {
        return userRepository.findByIdForUpdate(userId).orElseThrow();
    }

    private void replace(User user, Set<SpecialEligibilityType> requested) {
        List<UserSpecialEligibility> existing = eligibilityRepository.findAllByUser(user);
        eligibilityRepository.deleteAll(existing);
        eligibilityRepository.saveAll(requested.stream()
                .map(type -> UserSpecialEligibility.create(user, type))
                .toList());
        eligibilityRepository.flush();
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(3, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Latch timeout");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
