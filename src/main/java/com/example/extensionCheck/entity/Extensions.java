package com.example.extensionCheck.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Extensions")
public class Extensions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    @Column(nullable = false, name="is_active")
    private boolean isActive;    // true: 활성화, false: 비활성화

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ExtensionType type;

    @Builder
    public Extensions(String name, boolean isActive, ExtensionType type) {
        this.name = name;
        this.isActive = isActive;
        this.type = type;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

}
