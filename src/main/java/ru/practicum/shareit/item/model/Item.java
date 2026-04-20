package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

@Entity
@Table(name = "items")
@Setter
@Getter
@ToString
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id != null && id.equals(((Item) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
