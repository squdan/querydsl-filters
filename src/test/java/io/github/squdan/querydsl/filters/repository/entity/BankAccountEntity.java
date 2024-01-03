package io.github.squdan.querydsl.filters.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Builder
@Table(name = "bank_accounts")
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountEntity {

    @EmbeddedId
    private BankAccountId id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column
    private BigDecimal amount;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdOn;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedOn;
}
