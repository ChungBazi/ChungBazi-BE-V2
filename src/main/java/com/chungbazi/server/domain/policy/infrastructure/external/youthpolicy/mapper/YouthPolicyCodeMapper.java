package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import com.chungbazi.server.domain.policy.domain.type.EducationCode;
import com.chungbazi.server.domain.policy.domain.type.EmploymentCode;
import org.springframework.stereotype.Component;

@Component
public class YouthPolicyCodeMapper {

    public EducationCode toEducationCode(String schoolCd) {
        return switch (YouthPolicyTextUtils.trimToNull(schoolCd)) {
            case "0049002", "0049003" -> EducationCode.HIGH_SCHOOL_ATTENDING; //고교재학 & 고졸 예정 -> 고교 재학
            case "0049004" -> EducationCode.HIGH_SCHOOL_GRADUATED_NOT_ATTENDING; //고교 졸업 -> 고교 졸업(대학 미진학)
            case "0049005","0049006" -> EducationCode.UNIVERSITY_ATTENDING_OR_ON_LEAVE; //대학 재학 & 대졸 예정 -> 대학 재학
            case "0049007" -> EducationCode.UNIVERSITY_GRADUATED;  //대학 졸업 -> 대학 졸업
            case "0049008" -> EducationCode.GRADUATE_SCHOOL_GRADUATED; //석/박사 -> 대학원 졸업
            case "0049001", "0049009" -> EducationCode.ETC_OR_NONE; //기타 -> 기타/해당없음
            case "0049010" -> EducationCode.NONE_RESTRICT; //제한없음
            case null, default -> null;
        };
    }

    public EmploymentCode toEmploymentCode(String jobCd) {
        return switch (YouthPolicyTextUtils.trimToNull(jobCd)) {
            case "0013001" -> EmploymentCode.EMPLOYED; //재직자
            case "0013002", "0013006" -> EmploymentCode.SELF_EMPLOYED; //자영업자, (예비)창업자 -> 자영업/사업
            case "0013003" -> EmploymentCode.UNEMPLOYED; //미취업자
            case "0013004" -> EmploymentCode.FREELANCER; //프리랜서
            case "0013005", "0013007" -> EmploymentCode.TEMPORARY_DAILY_WORKER; //일용근로자, 단기근로자 -> 단기/일용 근로
            case "0013008", "0013009" -> EmploymentCode.ETC_OR_NONE; //영농종사자, 기타 -> 기타/해당없음
            case "0013010" -> EmploymentCode.NONE_RESTRICT; //제한없음
            case null, default -> null;
        };
    }
}
