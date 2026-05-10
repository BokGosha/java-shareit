package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentRepositoryTest {

    private final TestEntityManager em;

    private final CommentRepository commentRepository;

    private User author;
    private Item drill;
    private Item saw;

    @BeforeEach
    public void setUp() {
        User owner = newUser("Owner", "owner@example.com");
        author = newUser("Author", "author@example.com");
        drill = newItem("Drill", owner);
        saw = newItem("Saw", owner);
    }

    @Test
    @DisplayName("findAllByItem_Id: returns only comments for the given item")
    public void findAllByItemId_returnsOnlyForGivenItem() {
        Comment drillComment = newComment("Nice drill", author, drill);
        newComment("Sharp saw", author, saw);
        em.flush();

        List<Comment> result = commentRepository.findAllByItem_Id(drill.getId());

        assertThat(result).extracting(Comment::getId).containsExactly(drillComment.getId());
    }

    @Test
    @DisplayName("findAllByItem_IdIn: returns comments for any of the given item ids")
    public void findAllByItemIdIn_returnsForAllItems() {
        Comment drillComment = newComment("Nice drill", author, drill);
        Comment sawComment = newComment("Sharp saw", author, saw);
        em.flush();

        List<Comment> result = commentRepository.findAllByItem_IdIn(List.of(drill.getId(), saw.getId()));

        assertThat(result).extracting(Comment::getId)
                .containsExactlyInAnyOrder(drillComment.getId(), sawComment.getId());
    }

    @Test
    @DisplayName("findAllByItem_Id: returns empty list when no comments exist")
    public void findAllByItemId_returnsEmpty() {
        List<Comment> result = commentRepository.findAllByItem_Id(drill.getId());
        assertThat(result).isEmpty();
    }

    private User newUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        return em.persist(u);
    }

    private Item newItem(String name, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("desc");
        item.setIsAvailable(true);
        item.setOwner(owner);
        return em.persist(item);
    }

    private Comment newComment(String text, User author, Item item) {
        Comment c = new Comment();
        c.setText(text);
        c.setAuthor(author);
        c.setItem(item);
        c.setCreated(LocalDateTime.now());
        return em.persist(c);
    }
}
