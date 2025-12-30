package chungbazi.chungbazi_be.domain.user.validator;

import chungbazi.chungbazi_be.domain.user.dto.UserRequestDTO;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.repository.UserRepository;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateCharacterLevel(User user, UserRequestDTO.ProfileUpdateDto dto) {
        if (dto.getCharacterImg().getLevel() > user.getReward().getLevel()) {
            throw new BadRequestHandler(ErrorStatus.INVALID_CHARACTER);
        }
    }

    public void validateNickname(User user, UserRequestDTO.ProfileUpdateDto dto) {
        if (user.getName().equals(dto.getName())) {
            return;
        }

        if (userRepository.existsByName(dto.getName())) {
            throw new BadRequestHandler(ErrorStatus.INVALID_NICKNAME);
        }
    }
}
