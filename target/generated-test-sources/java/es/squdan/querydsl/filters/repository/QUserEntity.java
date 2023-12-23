package es.squdan.querydsl.filters.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserEntity is a Querydsl query type for UserEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEntity extends EntityPathBase<UserEntity> {

    private static final long serialVersionUID = -1047707718L;

    public static final QUserEntity userEntity = new QUserEntity("userEntity");

    public final DateTimePath<java.time.Instant> createdOn = createDateTime("createdOn", java.time.Instant.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final StringPath lastName = createString("lastName");

    public final DateTimePath<java.time.Instant> lastUpdatedOn = createDateTime("lastUpdatedOn", java.time.Instant.class);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final EnumPath<Roles> role = createEnum("role", Roles.class);

    public final NumberPath<java.math.BigDecimal> savings = createNumber("savings", java.math.BigDecimal.class);

    public final StringPath username = createString("username");

    public QUserEntity(String variable) {
        super(UserEntity.class, forVariable(variable));
    }

    public QUserEntity(Path<? extends UserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserEntity(PathMetadata metadata) {
        super(UserEntity.class, metadata);
    }

}

