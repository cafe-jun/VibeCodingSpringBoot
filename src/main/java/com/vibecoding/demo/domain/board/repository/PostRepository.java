package com.vibecoding.demo.domain.board.repository;

import com.vibecoding.demo.domain.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT * FROM posts ORDER BY id DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> findPostsWithOffsetAndLimit(@Param("offset") int offset, @Param("limit") int limit);
}
