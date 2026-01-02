package chungbazi.chungbazi_be.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 305651281L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final DateTimePath<java.time.LocalDateTime> blacklistedAt = createDateTime("blacklistedAt", java.time.LocalDateTime.class);

    public final EnumPath<chungbazi.chungbazi_be.domain.user.entity.enums.RewardLevel> characterImg = createEnum("characterImg", chungbazi.chungbazi_be.domain.user.entity.enums.RewardLevel.class);

    public final ListPath<chungbazi.chungbazi_be.domain.character.entity.Character, chungbazi.chungbazi_be.domain.character.entity.QCharacter> characters = this.<chungbazi.chungbazi_be.domain.character.entity.Character, chungbazi.chungbazi_be.domain.character.entity.QCharacter>createList("characters", chungbazi.chungbazi_be.domain.character.entity.Character.class, chungbazi.chungbazi_be.domain.character.entity.QCharacter.class, PathInits.DIRECT2);

    public final ListPath<chungbazi.chungbazi_be.domain.chat.entity.ChatRoomSetting, chungbazi.chungbazi_be.domain.chat.entity.QChatRoomSetting> chatRoomSettings = this.<chungbazi.chungbazi_be.domain.chat.entity.ChatRoomSetting, chungbazi.chungbazi_be.domain.chat.entity.QChatRoomSetting>createList("chatRoomSettings", chungbazi.chungbazi_be.domain.chat.entity.ChatRoomSetting.class, chungbazi.chungbazi_be.domain.chat.entity.QChatRoomSetting.class, PathInits.DIRECT2);

    public final ListPath<chungbazi.chungbazi_be.domain.community.entity.Comment, chungbazi.chungbazi_be.domain.community.entity.QComment> comments = this.<chungbazi.chungbazi_be.domain.community.entity.Comment, chungbazi.chungbazi_be.domain.community.entity.QComment>createList("comments", chungbazi.chungbazi_be.domain.community.entity.Comment.class, chungbazi.chungbazi_be.domain.community.entity.QComment.class, PathInits.DIRECT2);

    public final EnumPath<chungbazi.chungbazi_be.domain.user.entity.enums.Education> education = createEnum("education", chungbazi.chungbazi_be.domain.user.entity.enums.Education.class);

    public final StringPath email = createString("email");

    public final EnumPath<chungbazi.chungbazi_be.domain.user.entity.enums.Employment> employment = createEnum("employment", chungbazi.chungbazi_be.domain.user.entity.enums.Employment.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<chungbazi.chungbazi_be.domain.user.entity.enums.Income> income = createEnum("income", chungbazi.chungbazi_be.domain.user.entity.enums.Income.class);

    public final BooleanPath isBlacklisted = createBoolean("isBlacklisted");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final ListPath<chungbazi.chungbazi_be.domain.chat.entity.Message, chungbazi.chungbazi_be.domain.chat.entity.QMessage> messages = this.<chungbazi.chungbazi_be.domain.chat.entity.Message, chungbazi.chungbazi_be.domain.chat.entity.QMessage>createList("messages", chungbazi.chungbazi_be.domain.chat.entity.Message.class, chungbazi.chungbazi_be.domain.chat.entity.QMessage.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final ListPath<chungbazi.chungbazi_be.domain.notification.entity.Notification, chungbazi.chungbazi_be.domain.notification.entity.QNotification> notificationList = this.<chungbazi.chungbazi_be.domain.notification.entity.Notification, chungbazi.chungbazi_be.domain.notification.entity.QNotification>createList("notificationList", chungbazi.chungbazi_be.domain.notification.entity.Notification.class, chungbazi.chungbazi_be.domain.notification.entity.QNotification.class, PathInits.DIRECT2);

    public final chungbazi.chungbazi_be.domain.notification.entity.QNotificationSetting notificationSetting;

    public final EnumPath<chungbazi.chungbazi_be.domain.user.entity.enums.OAuthProvider> oAuthProvider = createEnum("oAuthProvider", chungbazi.chungbazi_be.domain.user.entity.enums.OAuthProvider.class);

    public final StringPath password = createString("password");

    public final ListPath<chungbazi.chungbazi_be.domain.community.entity.Post, chungbazi.chungbazi_be.domain.community.entity.QPost> posts = this.<chungbazi.chungbazi_be.domain.community.entity.Post, chungbazi.chungbazi_be.domain.community.entity.QPost>createList("posts", chungbazi.chungbazi_be.domain.community.entity.Post.class, chungbazi.chungbazi_be.domain.community.entity.QPost.class, PathInits.DIRECT2);

    public final EnumPath<chungbazi.chungbazi_be.domain.user.entity.enums.Region> region = createEnum("region", chungbazi.chungbazi_be.domain.user.entity.enums.Region.class);

    public final NumberPath<Integer> reportCount = createNumber("reportCount", Integer.class);

    public final EnumPath<chungbazi.chungbazi_be.domain.user.entity.enums.RewardLevel> reward = createEnum("reward", chungbazi.chungbazi_be.domain.user.entity.enums.RewardLevel.class);

    public final BooleanPath surveyStatus = createBoolean("surveyStatus");

    public final ListPath<chungbazi.chungbazi_be.domain.user.entity.mapping.UserAddition, chungbazi.chungbazi_be.domain.user.entity.mapping.QUserAddition> userAdditionList = this.<chungbazi.chungbazi_be.domain.user.entity.mapping.UserAddition, chungbazi.chungbazi_be.domain.user.entity.mapping.QUserAddition>createList("userAdditionList", chungbazi.chungbazi_be.domain.user.entity.mapping.UserAddition.class, chungbazi.chungbazi_be.domain.user.entity.mapping.QUserAddition.class, PathInits.DIRECT2);

    public final ListPath<chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest, chungbazi.chungbazi_be.domain.user.entity.mapping.QUserInterest> userInterestList = this.<chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest, chungbazi.chungbazi_be.domain.user.entity.mapping.QUserInterest>createList("userInterestList", chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest.class, chungbazi.chungbazi_be.domain.user.entity.mapping.QUserInterest.class, PathInits.DIRECT2);

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.notificationSetting = inits.isInitialized("notificationSetting") ? new chungbazi.chungbazi_be.domain.notification.entity.QNotificationSetting(forProperty("notificationSetting"), inits.get("notificationSetting")) : null;
    }

}

