package es.squdan.querydsl.filters.repository;

import com.querydsl.core.types.dsl.EntityPathBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, QueryDslRepository<UserEntity, EntityPathBase<UserEntity>> {

}
