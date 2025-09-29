// src/main/java/org/example/repository/OlympiadRepository.java
package org.example.repository;

import org.example.entity.Olympiad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OlympiadRepository extends JpaRepository<Olympiad, Long> {
    Optional<Olympiad> findByName(String name);

    @Modifying
    @Query(value = "DELETE FROM user_olympiads WHERE olympiad_id = ?1", nativeQuery = true)
    void deleteRelationsByOlympiadId(Long olympiadId);
}