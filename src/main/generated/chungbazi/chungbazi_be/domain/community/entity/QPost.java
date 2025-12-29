package chungbazi.chungbazi_be.domain.community.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPost is a Querydsl query type for Post
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPost extends EntityPathBase<Post> {

    private static final long serialVersionUID = 51878728L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPost post = new QPost("post");

    public final chungbazi.chungbazi_be.global.entity.QBaseTimeEntity _super = new chungbazi.chungbazi_be.global.entity.QBaseTimeEntity(this);

    public final BooleanPath anonymous = createBoolean("anonymous");

    public final chungbazi.chungbazi_be.domain.user.entity.QUser author;

    public final EnumPath<chungbazi.chungbazi_be.domain.policy.entity.Category> category = createEnum("category", chungbazi.chungbazi_be.domain.policy.entity.Category.class);

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<String, StringPath> imageUrls = this.<String, StringPath>createList("imageUrls", String.class, StringPath.class, PathInits.DIRECT2);

    public final NumberPath<Integer> postLikes = createNumber("postLikes", Integer.class);

    public final NumberPath<Integer> reportCount = createNumber("reportCount", Integer.class);

    public final EnumPath<chungbazi.chungbazi_be.domain.report.entity.enums.ReportReason> reportReason = createEnum("reportReason", chungbazi.chungbazi_be.domain.report.entity.enums.ReportReason.class);

    public final EnumPath<ContentStatus> status = createEnum("status", ContentStatus.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> views = createNumber("views", Integer.class);

    public QPost(String variable) {
        this(Post.class, forVariable(variable), INITS);
    }

    public QPost(Path<? extends Post> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPost(PathMetadata metadata, PathInits inits) {
        this(Post.class, metadata, inits);
    }

    public QPost(Class<? extends Post> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new chungbazi.chungbazi_be.domain.user.entity.QUser(forProperty("author"), inits.get("author")) : null;
    }

}

