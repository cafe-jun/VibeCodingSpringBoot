package com.vibecoding.demo.domain.posts.repository;

import com.vibecoding.demo.domain.posts.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT * FROM posts ORDER BY id DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> findPostsWithOffsetAndLimit(@Param("offset") int offset, @Param("limit") int limit);

    List<Post> findByIdLessThanOrderByIdDesc(Long id, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.id DESC")
    List<Post> findLatestPosts(org.springframework.data.domain.Pageable pageable);
}
