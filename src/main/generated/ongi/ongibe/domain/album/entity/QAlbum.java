package ongi.ongibe.domain.album.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAlbum is a Querydsl query type for Album
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAlbum extends EntityPathBase<Album> {

    private static final long serialVersionUID = -398324240L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAlbum album = new QAlbum("album");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final StringPath name = createString("name");

    public final ListPath<Picture, QPicture> pictures = this.<Picture, QPicture>createList("pictures", Picture.class, QPicture.class, PathInits.DIRECT2);

    public final EnumPath<ongi.ongibe.domain.album.AlbumProcessState> processState = createEnum("processState", ongi.ongibe.domain.album.AlbumProcessState.class);

    public final QPicture thumbnailPicture;

    public final ListPath<UserAlbum, QUserAlbum> userAlbums = this.<UserAlbum, QUserAlbum>createList("userAlbums", UserAlbum.class, QUserAlbum.class, PathInits.DIRECT2);

    public QAlbum(String variable) {
        this(Album.class, forVariable(variable), INITS);
    }

    public QAlbum(Path<? extends Album> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAlbum(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAlbum(PathMetadata metadata, PathInits inits) {
        this(Album.class, metadata, inits);
    }

    public QAlbum(Class<? extends Album> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.thumbnailPicture = inits.isInitialized("thumbnailPicture") ? new QPicture(forProperty("thumbnailPicture"), inits.get("thumbnailPicture")) : null;
    }

}

