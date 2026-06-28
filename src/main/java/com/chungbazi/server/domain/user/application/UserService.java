package com.chungbazi.server.domain.user.application;

import com.chungbazi.server.domain.user.api.dto.UserOnboardingRequest;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserInterestRepository userInterestRepository;

    @Transactional
    public void saveUserOnboarding(User user, UserOnboardingRequest request) {
        user.saveUserOnboarding(
                request.name(),
                request.birth(),
                request.sidoCode(),
                request.sigunguCode(),
                request.educationCode(),
                request.employmentCode(),
                request.incomeLevel()
        );

        Set<UserInterest> userInterests = request.interestCategories().stream()
                .map(subCategory -> UserInterest.createUserInterest(user, subCategory))
                .collect(Collectors.toSet());

        userInterestRepository.saveAll(userInterests);

        // TODO: 온보딩 가중치 로직 추가
    }
}
