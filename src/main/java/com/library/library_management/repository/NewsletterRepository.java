package com.library.library_management.repository;

import com.library.library_management.entity.Newsletter;
import com.library.library_management.entity.enums.NewsletterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    Page<Newsletter> findByStatus(NewsletterStatus status, Pageable pageable);

    List<Newsletter> findByStatusOrderByCreatedAtDesc(NewsletterStatus status);

    Page<Newsletter> findAllByOrderByCreatedAtDesc(Pageable pageable);
}