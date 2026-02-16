package chungbazi.chungbazi_be.domain.policy.dto;

import chungbazi.chungbazi_be.domain.cart.entity.Cart;
import chungbazi.chungbazi_be.domain.document.dto.DocumentResponseDTO;
import chungbazi.chungbazi_be.domain.document.entity.CalendarDocument;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PolicyCalendarDetailResponse {

    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate endDate;
    private Integer dDay;
    private Long cartId;
    private Long policyId;
    private List<DocumentResponseDTO.DocumentDTO> documents;
    private String referenceDocuments;

    public static PolicyCalendarDetailResponse of(Cart cart, Policy policy, List<CalendarDocument> documents) {

        Integer dDay;

        if (policy.getEndDate() == null) {
            dDay = null;
        } else {
            dDay = (int) ChronoUnit.DAYS.between(LocalDate.now(), policy.getEndDate());
        }

        List<DocumentResponseDTO.DocumentDTO> documentDTOs = documents.stream()
                .map(doc -> new DocumentResponseDTO.DocumentDTO(doc.getId(), doc.getDocument(), doc.isChecked()))
                .toList();

        return PolicyCalendarDetailResponse.builder()
                .name(policy.getName())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .dDay(dDay)
                .cartId(cart.getId())
                .policyId(policy.getId())
                .documents(documentDTOs)
                .referenceDocuments(policy.getDocument())
                .build();
    }
}
