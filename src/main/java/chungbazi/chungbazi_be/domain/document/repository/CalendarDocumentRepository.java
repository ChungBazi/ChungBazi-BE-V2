package chungbazi.chungbazi_be.domain.document.repository;

import chungbazi.chungbazi_be.domain.cart.entity.Cart;
import chungbazi.chungbazi_be.domain.document.entity.CalendarDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CalendarDocumentRepository extends JpaRepository<CalendarDocument, Long> {
    void deleteByIdIn(List<Long> deleteIds);

    List<CalendarDocument> findAllByCart_Id(Long cartId);

    void deleteByCart_IdIn(List<Long> deleteList);

    void deleteByCart(Cart cart);

    @Modifying
    @Query("DELETE FROM CalendarDocument cd " +
            "WHERE cd.cart.id IN :cartIds")
    void deleteByCartIdIn(List<Long> cartIds);
}
