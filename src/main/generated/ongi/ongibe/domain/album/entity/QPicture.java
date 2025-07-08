package ongi.ongibe.domain.album.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPicture is a Querydsl query type for Picture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPicture extends EntityPathBase<Picture> {

    private static final long serialVersionUID = -194834369L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPicture picture = new QPicture("picture");

    public final QAlbum album;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> createdDate = createDate("createdDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDuplicated = createBoolean("isDuplicated");

    public final BooleanPath isShaky = createBoolean("isShaky");

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath pictureURL = createString("pictureURL");

    public final ongi.ongibe.domain.place.entity.QPlace place;

    public final NumberPath<Float> qualityScore = createNumber("qualityScore", Float.class);

    public final StringPath s3Key = createString("s3Key");

    public final StringPath tag = createString("tag");

    public final DateTimePath<java.time.LocalDateTime> takeAt = createDateTime("takeAt", java.time.LocalDateTime.class);

    public final ongi.ongibe.domain.user.entity.QUser user;

    public QPicture(String variable) {
        this(Picture.class, forVariable(variable), INITS);
    }

    public QPicture(Path<? extends Picture> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPicture(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPicture(PathMetadata metadata, PathInits inits) {
        this(Picture.class, metadata, inits);
    }

    public QPicture(Class<? extends Picture> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.album = inits.isInitialized("album") ? new QAlbum(forProperty("album"), inits.get("album")) : null;
        this.place = inits.isInitialized("place") ? new ongi.ongibe.domain.place.entity.QPlace(forProperty("place")) : null;
        this.user = inits.isInitialized("user") ? new ongi.ongibe.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

