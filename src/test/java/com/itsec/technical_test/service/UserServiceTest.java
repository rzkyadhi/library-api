package com.itsec.technical_test.service;

import com.itsec.technical_test.entity.Role;
import com.itsec.technical_test.entity.User;
import com.itsec.technical_test.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void findAllReturnsUsersFromRepository() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertThat(result).isEqualTo(users);
        verify(userRepository).findAll();
    }

    @Test
    void getReturnsUserWhenFound() {
        User user = new User().setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.get(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void getThrowsWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.get(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createEncodesPasswordAndSavesUser() {
        User user = new User().setId(99L).setPassword("plain");
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.create(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getPassword()).isEqualTo("encoded");
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void updateChangesFieldsAndEncodesPasswordWhenProvided() {
        User existing = new User().setId(1L).setPassword("old");
        User updated = new User()
                .setFullName("New Name")
                .setUsername("newuser")
                .setEmail("new@example.com")
                .setRole(Role.EDITOR)
                .setPassword("newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.update(1L, updated);

        assertThat(result.getFullName()).isEqualTo("New Name");
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getRole()).isEqualTo(Role.EDITOR);
        assertThat(result.getPassword()).isEqualTo("encoded");
    }

    @Test
    void updateWithoutPasswordKeepsExistingPassword() {
        User existing = new User().setId(1L).setPassword("old");
        User updated = new User()
                .setFullName("Other")
                .setUsername("user")
                .setEmail("other@example.com")
                .setRole(Role.VIEWER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.update(1L, updated);

        assertThat(result.getPassword()).isEqualTo("old");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteRemovesExistingUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenUserMissing() {
        when(userRepository.existsById(1L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.delete(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}

