package ongi.ongibe.domain.album.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserAlbum is a Querydsl query type for UserAlbum
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserAlbum extends EntityPathBase<UserAlbum> {

    private static final long serialVersionUID = -1081790363L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserAlbum userAlbum = new QUserAlbum("userAlbum");

    public final QAlbum album;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<ongi.ongibe.domain.album.UserAlbumRole> role = createEnum("role", ongi.ongibe.domain.album.UserAlbumRole.class);

    public final ongi.ongibe.domain.user.entity.QUser user;

    public QUserAlbum(String variable) {
        this(UserAlbum.class, forVariable(variable), INITS);
    }

    public QUserAlbum(Path<? extends UserAlbum> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserAlbum(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserAlbum(PathMetadata metadata, PathInits inits) {
        this(UserAlbum.class, metadata, inits);
    }

    public QUserAlbum(Class<? extends UserAlbum> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.album = inits.isInitialized("album") ? new QAlbum(forProperty("album"), inits.get("album")) : null;
        this.user = inits.isInitialized("user") ? new ongi.ongibe.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

