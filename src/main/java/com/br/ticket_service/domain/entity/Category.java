package com.br.ticket_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "sla_hours", nullable = false)
    private Integer slaHours;

    private Boolean active;

    @PrePersist
    @Override
    protected void onBaseCreate() {
        super.onBaseCreate();
        if (this.active == null) this.active = true;
    }
}