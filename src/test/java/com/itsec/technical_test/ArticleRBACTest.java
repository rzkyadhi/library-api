package com.itsec.technical_test;

import com.itsec.technical_test.entity.Article;
import com.itsec.technical_test.entity.Role;
import com.itsec.technical_test.entity.User;
import com.itsec.technical_test.repository.ArticleRepository;
import com.itsec.technical_test.repository.UserRepository;
import com.itsec.technical_test.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleRBACTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        articleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void contributorCannotDeleteArticle() throws Exception {
        User contributor = new User();
        contributor.setFullName("Contrib User");
        contributor.setUsername("contrib");
        contributor.setEmail("contrib@example.com");
        contributor.setPassword(passwordEncoder.encode("password"));
        contributor.setRole(Role.CONTRIBUTOR);
        userRepository.save(contributor);

        Article article = new Article();
        article.setTitle("Title");
        article.setContent("Content");
        article.setUser(contributor);
        articleRepository.save(article);

        String token = jwtService.generateAccessToken(contributor.getUsername());

        mockMvc.perform(delete("/articles/" + article.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotCreateArticle() throws Exception {
        User viewer = new User();
        viewer.setFullName("View User");
        viewer.setUsername("viewer");
        viewer.setEmail("viewer@example.com");
        viewer.setPassword(passwordEncoder.encode("password"));
        viewer.setRole(Role.VIEWER);
        userRepository.save(viewer);

        Article article = new Article();
        article.setTitle("Title");
        article.setContent("Content");

        String token = jwtService.generateAccessToken(viewer.getUsername());

        mockMvc.perform(post("/articles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(article)))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminCanDeleteAnyArticle() throws Exception {
        User contributor = new User();
        contributor.setFullName("Contrib User");
        contributor.setUsername("contrib2");
        contributor.setEmail("contrib2@example.com");
        contributor.setPassword(passwordEncoder.encode("password"));
        contributor.setRole(Role.CONTRIBUTOR);
        userRepository.save(contributor);

        Article article = new Article();
        article.setTitle("Title");
        article.setContent("Content");
        article.setUser(contributor);
        articleRepository.save(article);

        User admin = new User();
        admin.setFullName("Admin");
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.SUPER_ADMIN);
        userRepository.save(admin);

        String token = jwtService.generateAccessToken(admin.getUsername());

        mockMvc.perform(delete("/articles/" + article.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
