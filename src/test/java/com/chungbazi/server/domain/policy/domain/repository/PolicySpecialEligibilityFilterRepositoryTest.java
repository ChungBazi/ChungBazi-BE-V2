package com.chungbazi.server.domain.policy.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicySpecialEligibility;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.user.domain.type.SpecialEligibilityType;
import com.chungbazi.server.fixture.PolicyFixture;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "firebase.enabled=false")
@ActiveProfiles("test")
@Transactional
class PolicySpecialEligibilityFilterRepositoryTest {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicySpecialEligibilityRepository policySpecialEligibilityRepository;

    @Test
    @DisplayName("특별 자격 사용자는 NONE 정책과 자격이 일치하는 정책을 조회한다")
    void userWithSpecificEligibilitySeesNoneAndMatchingPolicies() {
        Policy nonePolicy = savePolicy("eligibility-none", SpecialEligibilityType.NONE);
        Policy womanPolicy = savePolicy("eligibility-woman", SpecialEligibilityType.WOMAN);
        Policy employeePolicy = savePolicy("eligibility-employee", SpecialEligibilityType.SME_EMPLOYEE);

        List<Policy> result = findEligiblePolicies(Set.of(SpecialEligibilityType.WOMAN));

        assertThat(result)
                .extracting(Policy::getId)
                .contains(nonePolicy.getId(), womanPolicy.getId())
                .doesNotContain(employeePolicy.getId());
    }

    @Test
    @DisplayName("해당 사항 없음 사용자는 NONE 정책만 조회한다")
    void userWithNoneEligibilitySeesOnlyNonePolicies() {
        Policy nonePolicy = savePolicy("none-user-none", SpecialEligibilityType.NONE);
        Policy womanPolicy = savePolicy("none-user-woman", SpecialEligibilityType.WOMAN);

        List<Policy> result = findEligiblePolicies(Set.of(SpecialEligibilityType.NONE));

        assertThat(result)
                .extracting(Policy::getId)
                .contains(nonePolicy.getId())
                .doesNotContain(womanPolicy.getId());
    }

    @Test
    @DisplayName("다중 자격 사용자는 NONE 정책과 하나 이상의 자격이 일치하는 정책을 조회한다")
    void userWithMultipleEligibilitiesSeesNoneAndAnyMatchingPolicy() {
        Policy nonePolicy = savePolicy("multi-none", SpecialEligibilityType.NONE);
        Policy womanPolicy = savePolicy("multi-woman", SpecialEligibilityType.WOMAN);
        Policy employeePolicy = savePolicy("multi-employee", SpecialEligibilityType.SME_EMPLOYEE);
        Policy farmerPolicy = savePolicy("multi-farmer", SpecialEligibilityType.FARMER);

        List<Policy> result = findEligiblePolicies(Set.of(
                SpecialEligibilityType.WOMAN,
                SpecialEligibilityType.SME_EMPLOYEE
        ));

        assertThat(result)
                .extracting(Policy::getId)
                .contains(
                        nonePolicy.getId(),
                        womanPolicy.getId(),
                        employeePolicy.getId()
                )
                .doesNotContain(farmerPolicy.getId());
    }

    private List<Policy> findEligiblePolicies(Set<SpecialEligibilityType> eligibilityTypes) {
        return policyRepository.findEligiblePolicies(
                null,
                RecruitmentStatus.CLOSED,
                null,
                null,
                eligibilityTypes
        );
    }

    private Policy savePolicy(String policyNumber, SpecialEligibilityType eligibilityType) {
        Policy policy = policyRepository.saveAndFlush(
                PolicyFixture.policy()
                        .id(null)
                        .policyNumber(policyNumber)
                        .build()
        );

        policySpecialEligibilityRepository.saveAndFlush(PolicySpecialEligibility.create(policy, eligibilityType));
        return policy;
    }
}
