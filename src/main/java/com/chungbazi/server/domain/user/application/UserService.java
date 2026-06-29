package com.chungbazi.server.domain.user.application;

import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.user.api.dto.UserNameRequest;
import com.chungbazi.server.domain.user.api.dto.UserOnboardingRequest;
import com.chungbazi.server.domain.user.api.dto.UserPolicyRequest;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        List<UserInterest> userInterests = request.interestCategories().stream()
                .map(subCategory -> UserInterest.createUserInterest(user, subCategory))
                .toList();

        userInterestRepository.saveAll(userInterests);

        // TODO: 온보딩 가중치 로직 추가
    }

    @Transactional
    public void updateUserName(User user, UserNameRequest request) {
        user.updateName(request.name());
    }

    @Transactional
    public void updateUserPolicy(User user, UserPolicyRequest request) {
        user.updateUserPolicy(
                request.birth(),
                request.sidoCode(),
                request.sigunguCode(),
                request.educationCode(),
                request.employmentCode(),
                request.incomeLevel()
        );
        updateUserInterests(user, request.interestCategories());

        // TODO: 온보딩 가중치 로직 추가
    }

    private void updateUserInterests(User user, Set<PolicySubCategoryType> requestedCategories) {
        List<UserInterest> existingInterests = userInterestRepository.findAllByUser(user);

        List<UserInterest> deleteTargets = existingInterests.stream()
                .filter(interest -> !requestedCategories.contains(interest.getSubCategory()))
                .toList();

        Set<PolicySubCategoryType> existingCategories = existingInterests.stream()
                .map(UserInterest::getSubCategory)
                .collect(Collectors.toSet());

        List<UserInterest> addTargets = requestedCategories.stream()
                .filter(category -> !existingCategories.contains(category))
                .map(category -> UserInterest.createUserInterest(user, category))
                .toList();

        userInterestRepository.deleteAll(deleteTargets);
        userInterestRepository.saveAll(addTargets);
    }
}
