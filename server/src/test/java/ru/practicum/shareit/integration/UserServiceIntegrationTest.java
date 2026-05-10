package ru.practicum.shareit.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceIntegrationTest {

    private final UserService userService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void createUser_persistsUserAndReturnsDto() {
        UserCreateDto dto = new UserCreateDto("Alice", "alice@example.com");

        UserDto created = userService.createUser(dto);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Alice");
        assertThat(created.email()).isEqualTo("alice@example.com");

        User stored = em.find(User.class, created.id());
        assertThat(stored).isNotNull();
        assertThat(stored.getName()).isEqualTo("Alice");
        assertThat(stored.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void createUser_whenEmailDuplicated_throwsAndDoesNotInsert() {
        userService.createUser(new UserCreateDto("Alice", "dup@example.com"));

        assertThatThrownBy(() -> userService.createUser(new UserCreateDto("Bob", "dup@example.com")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        Long count = em.createQuery(
                        "select count(u) from User u where u.email = :e", Long.class)
                .setParameter("e", "dup@example.com")
                .getSingleResult();
        assertThat(count).isEqualTo(1L);
    }
}
