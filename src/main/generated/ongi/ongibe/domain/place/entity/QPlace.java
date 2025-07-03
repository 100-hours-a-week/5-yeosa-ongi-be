package ongi.ongibe.domain.place.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlace is a Querydsl query type for Place
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlace extends EntityPathBase<Place> {

    private static final long serialVersionUID = 733218032L;

    public static final QPlace place = new QPlace("place");

    public final StringPath city = createString("city");

    public final StringPath district = createString("district");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<ongi.ongibe.domain.album.entity.Picture, ongi.ongibe.domain.album.entity.QPicture> pictures = this.<ongi.ongibe.domain.album.entity.Picture, ongi.ongibe.domain.album.entity.QPicture>createList("pictures", ongi.ongibe.domain.album.entity.Picture.class, ongi.ongibe.domain.album.entity.QPicture.class, PathInits.DIRECT2);

    public final StringPath town = createString("town");

    public QPlace(String variable) {
        super(Place.class, forVariable(variable));
    }

    public QPlace(Path<? extends Place> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlace(PathMetadata metadata) {
        super(Place.class, metadata);
    }

}

