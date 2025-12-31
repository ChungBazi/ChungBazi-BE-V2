package chungbazi.chungbazi_be.domain.user.repository.UserBlockRepository;

import chungbazi.chungbazi_be.domain.user.entity.QUserBlock;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.entity.UserBlock;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserBlockRepositoryCustomImpl implements UserBlockRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public boolean existsBlockBetweenUsers(Long user1Id, Long user2Id) {
        QUserBlock qUserBlock = QUserBlock.userBlock;

        return queryFactory
                .selectOne()
                .from(qUserBlock)
                .where(
                        qUserBlock.isActive.isTrue(),
                        qUserBlock.blocker.id.in(user1Id, user2Id),
                        qUserBlock.blocked.id.in(user1Id, user2Id)
                )
                .fetchFirst() != null;
    }

    @Override
    @Transactional
    public void block(Long blockerId, Long blockedId) {
        QUserBlock qUserBlock = QUserBlock.userBlock;

        long updated = queryFactory
                .update(qUserBlock)
                .set(qUserBlock.isActive, true)
                .where(
                        qUserBlock.blocker.id.eq(blockerId),
                        qUserBlock.blocked.id.eq(blockedId)
                )
                .execute();

        if (updated == 0) {
            UserBlock userBlock = UserBlock.builder()
                    .blocker(em.getReference(User.class, blockerId))
                    .blocked(em.getReference(User.class, blockedId))
                    .isActive(true)
                    .build();
            em.persist(userBlock);
        }
    }

    @Override
    @Transactional
    public void unblock(Long blockerId, Long blockedId) {
        QUserBlock qUserBlock = QUserBlock.userBlock;

        queryFactory
                .update(qUserBlock)
                .set(qUserBlock.isActive, false)
                .where(
                        qUserBlock.blocker.id.eq(blockerId),
                        qUserBlock.blocked.id.eq(blockedId),
                        qUserBlock.isActive.isTrue()
                )
                .execute();
    }
}
