package com.boldin.instructions.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "instruction_resources")
public class InstructionResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instruction_id", nullable = false)
    private Instruction instruction;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false, length = 512)
    private String url;
}
