package ongi.ongibe.domain.album.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPictureFaceCluster is a Querydsl query type for PictureFaceCluster
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPictureFaceCluster extends EntityPathBase<PictureFaceCluster> {

    private static final long serialVersionUID = -1364677858L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPictureFaceCluster pictureFaceCluster = new QPictureFaceCluster("pictureFaceCluster");

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final QFaceCluster faceCluster;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPicture picture;

    public QPictureFaceCluster(String variable) {
        this(PictureFaceCluster.class, forVariable(variable), INITS);
    }

    public QPictureFaceCluster(Path<? extends PictureFaceCluster> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPictureFaceCluster(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPictureFaceCluster(PathMetadata metadata, PathInits inits) {
        this(PictureFaceCluster.class, metadata, inits);
    }

    public QPictureFaceCluster(Class<? extends PictureFaceCluster> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.faceCluster = inits.isInitialized("faceCluster") ? new QFaceCluster(forProperty("faceCluster"), inits.get("faceCluster")) : null;
        this.picture = inits.isInitialized("picture") ? new QPicture(forProperty("picture"), inits.get("picture")) : null;
    }

}

