package com.itsec.technical_test.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itsec.technical_test.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
