package com.example.extensionCheck.repository;

import com.example.extensionCheck.entity.ExtensionType;
import com.example.extensionCheck.entity.Extensions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtensionsRepository extends JpaRepository<Extensions, Long> {

    Optional<Extensions> findByName(String extensionName);

    List<Extensions> findAllByIsActiveTrue();

    // 이름으로 찾되, 현재 활성화(true) 상태인 것만 조회
    Optional<Extensions> findByNameAndIsActiveTrue(String name);

    // 타입별 활성화된 확장자 조회
    List<Extensions> findAllByTypeAndIsActiveTrue(ExtensionType type);

    // 이름과 타입으로 조회
    Optional<Extensions> findByNameAndType(String name, ExtensionType type);

    // 타입별 활성화된 확장자 개수 조회
    long countByTypeAndIsActiveTrue(ExtensionType type);

}
