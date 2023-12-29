package es.squdan.querydsl.filters.repository;

import com.querydsl.core.types.dsl.EntityPathBase;
import es.squdan.querydsl.filters.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, QueryDslRepository<UserEntity, EntityPathBase<UserEntity>> {

    // Configuration
    QueryDslRolesTypeManager CUSTOM_TYPES = new QueryDslRolesTypeManager();

    default QueryDslRolesTypeManager getCustomTypesManager() {
        return CUSTOM_TYPES;
    }

    default Class<UserEntity> getEntityType() {
        return UserEntity.class;
    }

}
