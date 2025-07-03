package ongi.ongibe.domain.album.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAlbumLike is a Querydsl query type for AlbumLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAlbumLike extends EntityPathBase<AlbumLike> {

    private static final long serialVersionUID = -1144145497L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAlbumLike albumLike = new QAlbumLike("albumLike");

    public final QAlbum album;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ongi.ongibe.domain.user.entity.QUser user;

    public QAlbumLike(String variable) {
        this(AlbumLike.class, forVariable(variable), INITS);
    }

    public QAlbumLike(Path<? extends AlbumLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAlbumLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAlbumLike(PathMetadata metadata, PathInits inits) {
        this(AlbumLike.class, metadata, inits);
    }

    public QAlbumLike(Class<? extends AlbumLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.album = inits.isInitialized("album") ? new QAlbum(forProperty("album"), inits.get("album")) : null;
        this.user = inits.isInitialized("user") ? new ongi.ongibe.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

