package chungbazi.chungbazi_be.domain.character.service;

import chungbazi.chungbazi_be.domain.character.converter.CharacterConverter;
import chungbazi.chungbazi_be.domain.character.dto.CharacterResponseDTO;
import chungbazi.chungbazi_be.domain.character.dto.CharacterResponseDTO.NextLevelInfo;
import chungbazi.chungbazi_be.domain.character.entity.Character;
import chungbazi.chungbazi_be.domain.character.repository.CharacterRepository;
import chungbazi.chungbazi_be.domain.community.service.RewardService;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.entity.enums.RewardLevel;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CharacterService {
    private final CharacterRepository characterRepository;
    private final RewardService rewardService;
    private final UserHelper userHelper;

    public List<CharacterResponseDTO.CharacterListDto> getCharacters() {
        User user = userHelper.getAuthenticatedUser();
        List<Character> characterList = characterRepository.findByUserId(user.getId());
        return CharacterConverter.toCharacterListDto(user, characterList);
    }

    public CharacterResponseDTO.MainCharacterDto selectOrOpen(RewardLevel selectedLevel) {
        User user = userHelper.getAuthenticatedUser();

        // 유저 레벨보다 높은 캐릭터 선택 시 에러 핸들링
        if (selectedLevel.getLevel() > user.getReward().getLevel()) {
            throw new BadRequestHandler(ErrorStatus.INVALID_CHARACTER);
        }

        Character character = characterRepository.findByUserIdAndRewardLevel(user.getId(), selectedLevel)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_CHARACTER));

        // 캐릭터 잠겨있는 경우 오픈
        if (!character.isOpen()) {
            character.setOpen(true);
            characterRepository.save(character);
        }
        NextLevelInfo nextLevelInfo = rewardService.calNextLevelInfo(user, character);
        return CharacterConverter.toMainCharacterDto(character, user, nextLevelInfo);
    }

    public CharacterResponseDTO.MainCharacterDto getMainCharacter() {
        User user = userHelper.getAuthenticatedUser();

        Character character = characterRepository.findTopByUserIdAndOpenOrderByRewardLevelDesc(user.getId(), true)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_CHARACTER));

        NextLevelInfo nextLevelInfo = rewardService.calNextLevelInfo(user, character);

        return CharacterConverter.toMainCharacterDto(character, user, nextLevelInfo);
    }
}
