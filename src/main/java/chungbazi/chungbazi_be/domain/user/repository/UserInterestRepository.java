package chungbazi.chungbazi_be.domain.user.repository;

import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    void deleteByUser(User user);

    @Query("SELECT ui FROM UserInterest ui " +
            "LEFT JOIN FETCH ui.interest " +
            "WHERE ui.user = :user ")
    List<UserInterest> findAllByUser(@Param("user") User user);
}
