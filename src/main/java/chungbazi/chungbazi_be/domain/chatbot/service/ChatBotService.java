package chungbazi.chungbazi_be.domain.chatbot.service;

import chungbazi.chungbazi_be.domain.chatbot.converter.ChatBotConverter;
import chungbazi.chungbazi_be.domain.chatbot.dto.ChatBotResponseDTO;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import chungbazi.chungbazi_be.domain.policy.repository.PolicyRepository;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatBotService {
    private final PolicyRepository policyRepository;
    private final ChatGptClient chatGptClient;

    public static final Set<String> VALID_KEYWORDS = Set.of(
            "계속", "상시", "매년", "연 2회", "별도 종료 시기 없음", "당해 연도", "상시 접속 가능"
    );

    public List<ChatBotResponseDTO.PolicyDto> getPolicies(Category category) {
        List<Policy> policies = policyRepository.findTop5ByCategoryOrderByCreatedAtDesc(category);

        LocalDate today = LocalDate.now();
        Set<String> validKeywords = VALID_KEYWORDS;

        return policies.stream()
                .filter(policy -> {
                            String status = policy.getStatus(today, validKeywords);
                            return !status.equals("마감");
                        })
                .limit(5)
                .map(policy -> ChatBotConverter.toPolicyDto(policy, today, validKeywords))
                .toList();
    }

    public ChatBotResponseDTO.PolicyDetailDto getPolicyDetails(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.POLICY_NOT_FOUND));

        LocalDate today = LocalDate.now();
        Set<String> validKeywords = VALID_KEYWORDS;

        return ChatBotConverter.toPolicyDetailDto(policy, today, validKeywords);
    }

    public ChatBotResponseDTO.ChatDto askGpt(String userMessage){
        if (isMeaningless(userMessage)) {
            return ChatBotConverter.toChatDto("죄송해요, 정책과 관련된 질문을 해주세요.");
        }
        String systemPrompt = "당신은 청년 정책을 설명해주는 챗봇입니다."; // 프롬프트 정적 지정
        String answer = chatGptClient.askChatGpt(userMessage, systemPrompt)
                .block();
        return ChatBotConverter.toChatDto(answer);
    }

    private boolean isMeaningless(String message){ //의미 없는 질문 방지
        if (message == null || message.trim().isEmpty()) return true;

        String trimmed = message.trim().replaceAll("[^ㄱ-ㅎ가-힣a-zA-Z0-9]", "");
        if (trimmed.length() < 3) return true;

        return false;
    }
}
