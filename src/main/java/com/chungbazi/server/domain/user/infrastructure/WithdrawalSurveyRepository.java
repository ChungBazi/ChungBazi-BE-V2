package com.chungbazi.server.domain.user.infrastructure;

import com.chungbazi.server.domain.user.domain.WithdrawalSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalSurveyRepository extends JpaRepository<WithdrawalSurvey, Long> {
}
