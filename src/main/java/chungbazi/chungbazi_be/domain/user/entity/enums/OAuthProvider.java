package chungbazi.chungbazi_be.domain.user.entity.enums;

public enum OAuthProvider {
    APPLE("애플"),
    KAKAO("카카오"),
    LOCAL("일반");

    public String description;

    OAuthProvider(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
