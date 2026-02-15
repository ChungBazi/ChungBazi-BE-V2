package chungbazi.chungbazi_be.domain.document.service;

import chungbazi.chungbazi_be.domain.cart.entity.Cart;
import chungbazi.chungbazi_be.domain.cart.service.CartService;
import chungbazi.chungbazi_be.domain.document.dto.DocumentRequestDTO;
import chungbazi.chungbazi_be.domain.document.dto.DocumentRequestDTO.DocumentUpdate;
import chungbazi.chungbazi_be.domain.document.entity.CalendarDocument;
import chungbazi.chungbazi_be.domain.document.repository.CalendarDocumentRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CalendarDocumentService {

    private final CalendarDocumentRepository calendarDocumentRepository;
    private final UserHelper userHelper;
    private final CartService cartService;


    // 서류 생성
    @Transactional
    public void addDocument(DocumentRequestDTO.DocumentCreateList dto, Long cartId) {

        User user = userHelper.getAuthenticatedUser();
        Cart cart = cartService.findById(cartId);

        dto.getDocuments().forEach(doc -> {
            CalendarDocument document = new CalendarDocument(doc, cart);
            calendarDocumentRepository.save(document);
        });
    }

    // 서류 수정
    @Transactional
    public void editDocument(Long cartId, List<DocumentRequestDTO.DocumentUpdate> dtos) {

        // 기존에 있는 서류
        List<CalendarDocument> documents = calendarDocumentRepository.findAllByCart_Id(cartId);
        Set<Long> documentIds = documents.stream().map(CalendarDocument::getId).collect(Collectors.toSet());

        Map<Long, DocumentUpdate> dtoMap = dtos.stream()
                .filter(dto -> dto.getDocumentId() != null)
                .collect(Collectors.toMap(DocumentRequestDTO.DocumentUpdate::getDocumentId, dto -> dto));

        // 삭제할 것들
        List<Long> deleteIds = documentIds.stream()
                .filter(id -> !dtoMap.containsKey(id))
                .toList();

        List<CalendarDocument> updateTargets = calendarDocumentRepository.findAllById(dtoMap.keySet());
        // 업데이트
        updateTargets.forEach(document -> {
            DocumentRequestDTO.DocumentUpdate dto = dtoMap.get(document.getId());
            document.updateDocument(dto.getContent());
        });

        // 삭제
        if (!deleteIds.isEmpty()) {
            calendarDocumentRepository.deleteByIdIn(deleteIds);
        }
    }

    // 서류 체크
    @Transactional
    public void checkDocument(Long cartId, List<DocumentRequestDTO.DocumentCheck> checkList) {

        checkList.forEach(check -> {
            CalendarDocument document = calendarDocumentRepository.findById(check.getDocumentId())
                    .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_DOCUMENT));

            document.updateCheck(check.isChecked());
        });
    }

    public List<CalendarDocument> findAllByCart_Id(Long cartId) {
        return calendarDocumentRepository.findAllByCart_Id(cartId);
    }


}
