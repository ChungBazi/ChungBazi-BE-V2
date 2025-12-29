package chungbazi.chungbazi_be.domain.policy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPolicy is a Querydsl query type for Policy
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPolicy extends EntityPathBase<Policy> {

    private static final long serialVersionUID = 308144095L;

    public static final QPolicy policy = new QPolicy("policy");

    public final chungbazi.chungbazi_be.global.entity.QBaseTimeEntity _super = new chungbazi.chungbazi_be.global.entity.QBaseTimeEntity(this);

    public final StringPath additionCondition = createString("additionCondition");

    public final StringPath applyProcedure = createString("applyProcedure");

    public final StringPath bizId = createString("bizId");

    public final StringPath bizPrdEtcCn = createString("bizPrdEtcCn");

    public final EnumPath<Category> category = createEnum("category", Category.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath document = createString("document");

    public final StringPath employment = createString("employment");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath incomeEtc = createString("incomeEtc");

    public final StringPath intro = createString("intro");

    public final StringPath maxAge = createString("maxAge");

    public final StringPath maxIncome = createString("maxIncome");

    public final StringPath minAge = createString("minAge");

    public final StringPath minIncome = createString("minIncome");

    public final StringPath name = createString("name");

    public final StringPath posterUrl = createString("posterUrl");

    public final StringPath referenceUrl1 = createString("referenceUrl1");

    public final StringPath referenceUrl2 = createString("referenceUrl2");

    public final StringPath registerUrl = createString("registerUrl");

    public final StringPath restrictedCondition = createString("restrictedCondition");

    public final StringPath result = createString("result");

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPolicy(String variable) {
        super(Policy.class, forVariable(variable));
    }

    public QPolicy(Path<? extends Policy> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPolicy(PathMetadata metadata) {
        super(Policy.class, metadata);
    }

}

