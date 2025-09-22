package com.boldin.instructions.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@Table(name = "instruction_sections")
public class InstructionSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instruction_id", nullable = false)
    private Instruction instruction;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, length = 180)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "code_title", length = 180)
    private String codeTitle;

    @Column(name = "code_language", length = 32)
    private String codeLanguage;

    @Lob
    @Column(name = "code_snippet")
    private String codeSnippet;

    @Column(name = "cta_label", length = 120)
    private String ctaLabel;

    @Column(name = "cta_url", length = 512)
    private String ctaUrl;
}
