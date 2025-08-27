package com.itsec.technical_test.controller;

import com.itsec.technical_test.entity.Article;
import com.itsec.technical_test.entity.User;
import com.itsec.technical_test.service.ArticleService;
import com.itsec.technical_test.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Articles")
public class ArticleController {
    private final ArticleService articleService;
    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List all articles")
    public ResponseEntity<List<Article>> all(@AuthenticationPrincipal User user,
                                             HttpServletRequest request) {
        List<Article> articles = articleService.findAll();
        auditLogService.log(user, "LIST_ARTICLES", null, request.getHeader("User-Agent"));
        return ResponseEntity.ok(articles);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','EDITOR','CONTRIBUTOR')")
    @Operation(summary = "Create a new article")
    public Article create(@Valid @RequestBody Article article,
                          @AuthenticationPrincipal User user,
                          HttpServletRequest request) {
        Article created = articleService.create(article, user);
        auditLogService.log(user, "CREATE_ARTICLE", "Article ID: " + created.getId(),
                request.getHeader("User-Agent"));
        return created;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an article by ID")
    public ResponseEntity<Article> get(@PathVariable Long id,
                                       @AuthenticationPrincipal User user,
                                       HttpServletRequest request) {
        Article article = articleService.get(id);
        auditLogService.log(user, "GET_ARTICLE", "Article ID: " + id, request.getHeader("User-Agent"));
        return ResponseEntity.ok(article);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','EDITOR','CONTRIBUTOR')")
    @Operation(summary = "Update an article")
    public ResponseEntity<Article> update(@PathVariable Long id, @RequestBody Article updated,
                                          @AuthenticationPrincipal User user,
                                          HttpServletRequest request) {
        Article article = articleService.update(id, updated, user);
        auditLogService.log(user, "UPDATE_ARTICLE", "Article ID: " + id, request.getHeader("User-Agent"));
        return ResponseEntity.ok(article);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','EDITOR')")
    @Operation(summary = "Delete an article")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal User user,
                                       HttpServletRequest request) {
        articleService.delete(id, user);
        auditLogService.log(user, "DELETE_ARTICLE", "Article ID: " + id,
                request.getHeader("User-Agent"));
        return ResponseEntity.noContent().build();
    }
}
