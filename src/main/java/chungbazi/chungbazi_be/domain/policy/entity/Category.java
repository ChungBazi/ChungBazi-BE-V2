package chungbazi.chungbazi_be.domain.policy.entity;

import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Category {

    JOBS("일자리", "001"),
    HOUSING("주거", "002"),
    EDUCATION("교육", "003"),
    WELFARE_CULTURE("복지·문화", "004"),
    PARTICIPATION_RIGHTS("참여·권리", "005");

    @Getter
    private final String koreanName;
    private final String code;

    public static Category fromCode(String code) {
        for (Category category : Category.values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new GeneralException(ErrorStatus.CATEGORY_CODE_NOT_FOUND);
    }

    public static Category fromKoreanName(String koreanName) {
        for (Category category : Category.values()) {
            if (category.koreanName.equals(koreanName)) {
                return category;
            }
        }
        throw new GeneralException(ErrorStatus.CATEGORY_NAME_NOT_FOUND);
    }
}