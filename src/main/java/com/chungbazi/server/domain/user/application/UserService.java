package com.chungbazi.server.domain.user.application;

import com.chungbazi.server.domain.auth.infrastructure.redis.RefreshTokenRepository;
import com.chungbazi.server.domain.notification.domain.repository.NotificationSettingRepository;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentSearchKeywordRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.user.api.dto.request.UserNameRequest;
import com.chungbazi.server.domain.user.api.dto.request.UserOnboardingRequest;
import com.chungbazi.server.domain.user.api.dto.response.UserOnboardingResponse;
import com.chungbazi.server.domain.user.api.dto.request.UserPolicyRequest;
import com.chungbazi.server.domain.user.api.dto.request.UserWithdrawalRequest;
import com.chungbazi.server.domain.user.api.dto.response.UserInfoResponse;
import com.chungbazi.server.domain.user.api.dto.response.UserPolicyResponse;
import com.chungbazi.server.domain.user.application.validator.UserValidator;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.domain.UserSpecialEligibility;
import com.chungbazi.server.domain.user.domain.WithdrawalSurvey;
import com.chungbazi.server.domain.user.domain.type.SpecialEligibilityType;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import com.chungbazi.server.domain.user.infrastructure.UserSpecialEligibilityRepository;
import com.chungbazi.server.domain.user.infrastructure.WithdrawalSurveyRepository;
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

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserSpecialEligibilityRepository userSpecialEligibilityRepository;
    private final PolicyLikeRepository policyLikeRepository;
    private final RecentViewedPolicyRepository recentViewedPolicyRepository;
    private final RecentSearchKeywordRepository recentSearchKeywordRepository;
    private final WithdrawalSurveyRepository withdrawalSurveyRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserValidator userValidator;

    @Transactional
    public UserOnboardingResponse saveUserOnboarding(User user, UserOnboardingRequest request) {
        userValidator.validateOnboarding(request);

        user.saveUserOnboarding(
                request.birth(),
                request.sidoCode(),
                request.sigunguCode(),
                request.educationCode(),
                request.employmentCode(),
                request.incomeLevel()
        );
        updateUserInterests(user, request.interestCategories());
        updateUserSpecialEligibilities(user, request.specialEligibilities());

        return UserOnboardingResponse.from(user);
    }

    @Transactional
    public void updateUserName(User user, UserNameRequest request) {
        userValidator.validateName(request.name());
        user.updateName(request.name());
    }

    @Transactional
    public void updateUserPolicy(User user, UserPolicyRequest request) {
        userValidator.validatePolicy(request);

        user.updateUserPolicy(
                request.birth(),
                request.sidoCode(),
                request.sigunguCode(),
                request.educationCode(),
                request.employmentCode(),
                request.incomeLevel()
        );
        updateUserInterests(user, request.interestCategories());
        updateUserSpecialEligibilities(user, request.specialEligibilities());
    }

    public UserInfoResponse getUserInfo(User user) {
        return UserInfoResponse.from(user);
    }

    public UserPolicyResponse getUserPolicy(User user) {
        Set<PolicySubCategoryType> interestCategories = userInterestRepository.findAllByUser(user).stream()
                .map(UserInterest::getSubCategory)
                .collect(Collectors.toSet());

        Set<SpecialEligibilityType> specialEligibilities =
                userSpecialEligibilityRepository.findAllByUser(user).stream()
                        .map(UserSpecialEligibility::getEligibilityType)
                        .collect(Collectors.toSet());

        return UserPolicyResponse.of(user, interestCategories, specialEligibilities);
    }

    @Transactional
    public void withdrawUser(User user, UserWithdrawalRequest request) {
        saveWithdrawalSurvey(request);
        deleteAuthenticationData(user);
        deleteUserActivity(user);
        deleteNotificationSetting(user);
        deleteUser(user);
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

    private void updateUserSpecialEligibilities(User user, Set<SpecialEligibilityType> requestedEligibilities) {
        List<UserSpecialEligibility> existingEligibilities = userSpecialEligibilityRepository.findAllByUser(user);

        List<UserSpecialEligibility> deleteTargets = existingEligibilities.stream()
                .filter(eligibility -> !requestedEligibilities.contains(eligibility.getEligibilityType()))
                .toList();

        Set<SpecialEligibilityType> existingTypes = existingEligibilities.stream()
                        .map(UserSpecialEligibility::getEligibilityType)
                        .collect(Collectors.toSet());

        List<UserSpecialEligibility> addTargets = requestedEligibilities.stream()
                .filter(type -> !existingTypes.contains(type))
                .map(type -> UserSpecialEligibility.create(user, type))
                .toList();

        userSpecialEligibilityRepository.deleteAll(deleteTargets);
        userSpecialEligibilityRepository.saveAll(addTargets);
    }

    private void saveWithdrawalSurvey(UserWithdrawalRequest request) {
        WithdrawalSurvey survey = WithdrawalSurvey.create(
                request.reasons(),
                request.detail()
        );
        withdrawalSurveyRepository.save(survey);
    }

    private void deleteAuthenticationData(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    private void deleteUserActivity(User user) {
        userInterestRepository.deleteAllByUserId(user.getId());
        userSpecialEligibilityRepository.deleteAllByUserId(user.getId());
        policyLikeRepository.deleteAllByUserId(user.getId());
        recentViewedPolicyRepository.deleteAllByUserId(user.getId());
        recentSearchKeywordRepository.deleteAllByUserId(user.getId());
    }

    private void deleteNotificationSetting(User user) {
        notificationSettingRepository.deleteAllByUserId(user.getId());
    }

    private void deleteUser(User user) {
        userRepository.delete(user);
    }
}
