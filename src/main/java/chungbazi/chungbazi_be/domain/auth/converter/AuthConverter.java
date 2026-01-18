package chungbazi.chungbazi_be.domain.auth.converter;

import chungbazi.chungbazi_be.domain.auth.dto.TokenDTO;
import chungbazi.chungbazi_be.domain.auth.dto.TokenResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AuthConverter {

    public TokenResponseDTO.LoginTokenResponseDTO toLoginTokenResponse(TokenDTO token) {
        return TokenResponseDTO.LoginTokenResponseDTO.of(
                token.getUserId(),
                token.getUserName(),
                token.getIsFirst(),
                token.getAccessToken(),
                token.getRefreshToken(),
                token.getAccessExp()
        );
    }

    public TokenResponseDTO.RefreshTokenResponseDTO toRefreshTokenResponse(TokenDTO token) {
        return TokenResponseDTO.RefreshTokenResponseDTO.of(
                token.getUserId(),
                token.getUserName(),
                token.getAccessToken(),
                token.getRefreshToken(),
                token.getAccessExp()
        );
    }
}
