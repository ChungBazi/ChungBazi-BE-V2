package com.chungbazi.server.domain.policy.domain.entity;

import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "policy_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_detail_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false, unique = true)
    private Policy policy;

    @Column(name = "eligibility_description", columnDefinition = "text")
    private String eligibilityDescription;

    @Column(name = "application_method", columnDefinition = "text")
    private String applicationMethod;

    @Column(name = "submitted_document", columnDefinition = "text")
    private String submittedDocument;

    @Column(name = "screening_method", columnDefinition = "text")
    private String screeningMethod;

    @Column(name = "notice", columnDefinition = "text")
    private String notice;

    @Column(name = "apply_url", columnDefinition = "text")
    private String applyUrl;

    @Column(name = "reference_url1", columnDefinition = "text")
    private String referenceUrl1;

    @Column(name = "reference_url2", columnDefinition = "text")
    private String referenceUrl2;

    public static PolicyDetail createPolicyDetail(
            Policy policy,
            String eligibilityDescription,
            String applicationMethod,
            String submittedDocument,
            String screeningMethod,
            String notice,
            String applyUrl,
            String referenceUrl1,
            String referenceUrl2
    ) {
        PolicyDetail policyDetail = new PolicyDetail();
        policyDetail.policy = policy;
        policyDetail.eligibilityDescription = eligibilityDescription;
        policyDetail.applicationMethod = applicationMethod;
        policyDetail.submittedDocument = submittedDocument;
        policyDetail.screeningMethod = screeningMethod;
        policyDetail.notice = notice;
        policyDetail.applyUrl = applyUrl;
        policyDetail.referenceUrl1 = referenceUrl1;
        policyDetail.referenceUrl2 = referenceUrl2;
        return policyDetail;
    }
}
