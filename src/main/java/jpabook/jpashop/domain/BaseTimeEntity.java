package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass // 엔티티 상속 클래스 설정
@EntityListeners(value = AuditingEntityListener.class)
@Slf4j
@Getter
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    /**
     * @PrePersist
     * 엔티티가 저장되는 시점에 호출
     */
    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        log.info("PrePersist now: {}", now);
        // createdDate = now;
        // lastModifiedDate = now;
    }

    /**
     * @PreUpdate
     * 엔티티가 업데이트되는 시점에 호출
     */
    @PreUpdate
    private void preUpdate() {
        LocalDateTime now = LocalDateTime.now();
        log.info("PreUpdate now: {}", now);
        // lastModifiedDate = now;
    }
}
