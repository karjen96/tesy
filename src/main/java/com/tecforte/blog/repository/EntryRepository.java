package com.tecforte.blog.repository;

import com.tecforte.blog.domain.Blog;
import com.tecforte.blog.domain.Entry;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for the Entry entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

	Optional<Entry> findOneByBlog(Long id);

	List<Entry> findByContentIgnoreCaseContainingOrTitleIgnoreCaseContaining(String keywords1, String keywords2);
}
