package com.chungbazi.server.domain.policy.domain.type;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SidoCode {
    SEOUL("11", "서울특별시"),
    BUSAN("26", "부산광역시"),
    DAEGU("27", "대구광역시"),
    INCHEON("28", "인천광역시"),
    GWANGJU("29", "광주광역시"),
    DAEJEON("30", "대전광역시"),
    ULSAN("31", "울산광역시"),
    SEJONG("36", "세종특별자치시"),
    GYEONGGI("41", "경기도"),
    GANGWON("51", "강원특별자치도"),
    CHUNGBUK("43", "충청북도"),
    CHUNGNAM("44", "충청남도"),
    JEONBUK("52", "전북특별자치도"),
    JEONNAM("46", "전라남도"),
    GYEONGBUK("47", "경상북도"),
    GYEONGNAM("48", "경상남도"),
    JEJU("50", "제주특별자치도");

    private final String code;
    private final String name;

    public static SidoCode fromSigunguCode(String sigunguCode) {
        if (sigunguCode == null || sigunguCode.length() < 2) {
            return null;
        }

        String prefix = sigunguCode.substring(0, 2);
        if (!prefix.chars().allMatch(Character::isDigit)) {
            return null;
        }

        return fromCode(prefix);
    }

    public static SidoCode fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(sidoCode -> sidoCode.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
