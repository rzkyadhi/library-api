package com.itsec.technical_test.service;

import com.itsec.technical_test.entity.Article;
import com.itsec.technical_test.entity.Role;
import com.itsec.technical_test.entity.User;
import com.itsec.technical_test.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    public Article create(Article article, User user) {
        if (user.getRole() == Role.VIEWER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        article.setId(null).setUser(user);
        return articleRepository.save(article);
    }

    public Article get(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Article update(Long id, Article updated, User user) {
        var article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user.getRole() != Role.SUPER_ADMIN &&
                !article.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (user.getRole() == Role.VIEWER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        article.setTitle(updated.getTitle())
                .setContent(updated.getContent());
        return articleRepository.save(article);
    }

    public void delete(Long id, User user) {
        var article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user.getRole() == Role.SUPER_ADMIN) {
            articleRepository.delete(article);
            return;
        }
        if (user.getRole() != Role.EDITOR ||
                !article.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        articleRepository.delete(article);
    }
}

