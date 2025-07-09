package ongi.ongibe.domain.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOAuthToken is a Querydsl query type for OAuthToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOAuthToken extends EntityPathBase<OAuthToken> {

    private static final long serialVersionUID = -630571116L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOAuthToken oAuthToken = new QOAuthToken("oAuthToken");

    public final StringPath accessToken = createString("accessToken");

    public final DateTimePath<java.time.LocalDateTime> accessTokenExpiresAt = createDateTime("accessTokenExpiresAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<ongi.ongibe.domain.auth.OAuthProvider> provider = createEnum("provider", ongi.ongibe.domain.auth.OAuthProvider.class);

    public final StringPath refreshToken = createString("refreshToken");

    public final DateTimePath<java.time.LocalDateTime> refreshTokenExpiresAt = createDateTime("refreshTokenExpiresAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final ongi.ongibe.domain.user.entity.QUser user;

    public QOAuthToken(String variable) {
        this(OAuthToken.class, forVariable(variable), INITS);
    }

    public QOAuthToken(Path<? extends OAuthToken> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOAuthToken(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOAuthToken(PathMetadata metadata, PathInits inits) {
        this(OAuthToken.class, metadata, inits);
    }

    public QOAuthToken(Class<? extends OAuthToken> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new ongi.ongibe.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

