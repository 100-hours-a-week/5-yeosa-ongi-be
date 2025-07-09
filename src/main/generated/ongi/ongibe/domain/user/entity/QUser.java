package ongi.ongibe.domain.user.entity;

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

    private static final long serialVersionUID = -441765632L;

    public static final QUser user = new QUser("user");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nickname = createString("nickname");

    public final ListPath<ongi.ongibe.domain.notification.entity.Notification, ongi.ongibe.domain.notification.entity.QNotification> notifications = this.<ongi.ongibe.domain.notification.entity.Notification, ongi.ongibe.domain.notification.entity.QNotification>createList("notifications", ongi.ongibe.domain.notification.entity.Notification.class, ongi.ongibe.domain.notification.entity.QNotification.class, PathInits.DIRECT2);

    public final ListPath<ongi.ongibe.domain.auth.entity.OAuthToken, ongi.ongibe.domain.auth.entity.QOAuthToken> oAuthTokens = this.<ongi.ongibe.domain.auth.entity.OAuthToken, ongi.ongibe.domain.auth.entity.QOAuthToken>createList("oAuthTokens", ongi.ongibe.domain.auth.entity.OAuthToken.class, ongi.ongibe.domain.auth.entity.QOAuthToken.class, PathInits.DIRECT2);

    public final ListPath<ongi.ongibe.domain.album.entity.Picture, ongi.ongibe.domain.album.entity.QPicture> pictures = this.<ongi.ongibe.domain.album.entity.Picture, ongi.ongibe.domain.album.entity.QPicture>createList("pictures", ongi.ongibe.domain.album.entity.Picture.class, ongi.ongibe.domain.album.entity.QPicture.class, PathInits.DIRECT2);

    public final StringPath profileImage = createString("profileImage");

    public final EnumPath<ongi.ongibe.domain.auth.OAuthProvider> provider = createEnum("provider", ongi.ongibe.domain.auth.OAuthProvider.class);

    public final StringPath providerId = createString("providerId");

    public final StringPath s3Key = createString("s3Key");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final ListPath<ongi.ongibe.domain.album.entity.UserAlbum, ongi.ongibe.domain.album.entity.QUserAlbum> userAlbums = this.<ongi.ongibe.domain.album.entity.UserAlbum, ongi.ongibe.domain.album.entity.QUserAlbum>createList("userAlbums", ongi.ongibe.domain.album.entity.UserAlbum.class, ongi.ongibe.domain.album.entity.QUserAlbum.class, PathInits.DIRECT2);

    public final EnumPath<ongi.ongibe.domain.user.UserStatus> userStatus = createEnum("userStatus", ongi.ongibe.domain.user.UserStatus.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

