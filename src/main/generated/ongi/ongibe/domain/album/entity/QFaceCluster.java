package ongi.ongibe.domain.album.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFaceCluster is a Querydsl query type for FaceCluster
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFaceCluster extends EntityPathBase<FaceCluster> {

    private static final long serialVersionUID = 139037598L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFaceCluster faceCluster = new QFaceCluster("faceCluster");

    public final NumberPath<Integer> bboxX1 = createNumber("bboxX1", Integer.class);

    public final NumberPath<Integer> bboxX2 = createNumber("bboxX2", Integer.class);

    public final NumberPath<Integer> bboxY1 = createNumber("bboxY1", Integer.class);

    public final NumberPath<Integer> bboxY2 = createNumber("bboxY2", Integer.class);

    public final StringPath clusterName = createString("clusterName");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPicture representativePicture;

    public QFaceCluster(String variable) {
        this(FaceCluster.class, forVariable(variable), INITS);
    }

    public QFaceCluster(Path<? extends FaceCluster> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFaceCluster(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFaceCluster(PathMetadata metadata, PathInits inits) {
        this(FaceCluster.class, metadata, inits);
    }

    public QFaceCluster(Class<? extends FaceCluster> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.representativePicture = inits.isInitialized("representativePicture") ? new QPicture(forProperty("representativePicture"), inits.get("representativePicture")) : null;
    }

}

