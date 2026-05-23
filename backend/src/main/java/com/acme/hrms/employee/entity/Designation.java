package com.acme.hrms.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import com.acme.hrms.common.persistence.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "designation")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Designation extends BaseEntity {

    @Column(name = "title", nullable = false, columnDefinition = "citext")
    private String title;

    @Column(name = "level")
    private String level;

    @Column(name = "description")
    private String description;
}
