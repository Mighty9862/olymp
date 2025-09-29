package org.example.repository;

import org.example.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    @Query("SELECT n FROM News n ORDER BY n.newsDate DESC, n.createdAt DESC")
    List<News> findAllOrderByDateDesc();

    List<News> findByOrderByNewsDateDescCreatedAtDesc();
}