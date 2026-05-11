package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class MapperBranchCoverageTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);
    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);
    private final ItemRequestMapper itemRequestMapper = Mappers.getMapper(ItemRequestMapper.class);

    @Test
    void userMapper_returnsNullForNullInputs() {
        assertThat(userMapper.mapUserToUserDto(null)).isNull();
        assertThat(userMapper.mapUserCreateDtoToUser(null)).isNull();
        assertThat(userMapper.mapUserToUserIdDto(null)).isNull();

        User user = new User();
        user.setName("origName");
        user.setEmail("orig@example.com");

        userMapper.updateUserFields(user, null);
        assertThat(user.getName()).isEqualTo("origName");

        userMapper.updateUserFields(user, new UserUpdateDto(null, null));
        assertThat(user.getName()).isEqualTo("origName");
        assertThat(user.getEmail()).isEqualTo("orig@example.com");

        userMapper.updateUserFields(user, new UserUpdateDto("New", null));
        assertThat(user.getName()).isEqualTo("New");
        assertThat(user.getEmail()).isEqualTo("orig@example.com");

        userMapper.updateUserFields(user, new UserUpdateDto(null, "new@example.com"));
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void userMapper_mapsPopulatedUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Name");
        user.setEmail("e@example.com");

        assertThat(userMapper.mapUserToUserDto(user).id()).isEqualTo(1L);
        assertThat(userMapper.mapUserToUserIdDto(user).id()).isEqualTo(1L);
        assertThat(userMapper.mapUserCreateDtoToUser(new UserCreateDto("N", "x@example.com"))
                .getName()).isEqualTo("N");
    }

    @Test
    void itemMapper_returnsNullForNullInputs() {
        assertThat(itemMapper.mapItemToItemDto(null)).isNull();
        assertThat(itemMapper.mapItemToItemMoreDto(null)).isNull();
        assertThat(itemMapper.mapItemCreateDtoToItem(null)).isNull();
        assertThat(itemMapper.mapItemToItemShortDto(null)).isNull();
        assertThat(itemMapper.mapItemToItemRequestResponseDto(null)).isNull();

        Item item = new Item();
        item.setName("orig");
        item.setDescription("desc");
        item.setIsAvailable(true);

        itemMapper.updateItemFields(item, null);
        assertThat(item.getName()).isEqualTo("orig");

        itemMapper.updateItemFields(item, new ItemUpdateDto(null, null, null));
        assertThat(item.getName()).isEqualTo("orig");
        assertThat(item.getDescription()).isEqualTo("desc");
        assertThat(item.getIsAvailable()).isTrue();

        itemMapper.updateItemFields(item, new ItemUpdateDto("X", null, null));
        assertThat(item.getName()).isEqualTo("X");
        itemMapper.updateItemFields(item, new ItemUpdateDto(null, "Y", null));
        assertThat(item.getDescription()).isEqualTo("Y");
        itemMapper.updateItemFields(item, new ItemUpdateDto(null, null, false));
        assertThat(item.getIsAvailable()).isFalse();
    }

    @Test
    void itemMapper_mapsItemWithAndWithoutOwner() {
        Item item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setDescription("D");
        item.setIsAvailable(true);

        assertThat(itemMapper.mapItemToItemRequestResponseDto(item).ownerId()).isNull();

        User owner = new User();
        owner.setId(7L);
        item.setOwner(owner);

        assertThat(itemMapper.mapItemToItemRequestResponseDto(item).ownerId()).isEqualTo(7L);
        assertThat(itemMapper.mapItemToItemDto(item).id()).isEqualTo(10L);
        assertThat(itemMapper.mapItemToItemMoreDto(item).id()).isEqualTo(10L);
        assertThat(itemMapper.mapItemToItemShortDto(item).name()).isEqualTo("Drill");
        assertThat(itemMapper.mapItemCreateDtoToItem(new ItemCreateDto("N", "D", true, null))
                .getIsAvailable()).isTrue();
    }

    @Test
    void bookingMapper_returnsNullForNullInputs() {
        BookingMapper realMapper = createBookingMapper();
        assertThat(realMapper.mapBookingToBookingDto(null)).isNull();
        assertThat(realMapper.mapBookingCreateDtoToBooking(null)).isNull();
        assertThat(realMapper.mapBookingToBookingShortDto(null)).isNull();
    }

    @Test
    void bookingMapper_mapBookingToBookingShortDto_handlesNullItemAndBooker() {
        BookingMapper realMapper = createBookingMapper();
        Booking booking = new Booking();
        booking.setId(1L);

        var dto = realMapper.mapBookingToBookingShortDto(booking);
        assertThat(dto.itemId()).isNull();
        assertThat(dto.bookerId()).isNull();

        Item item = new Item();
        item.setId(2L);
        booking.setItem(item);

        User booker = new User();
        booker.setId(3L);
        booking.setBooker(booker);

        var dto2 = realMapper.mapBookingToBookingShortDto(booking);
        assertThat(dto2.itemId()).isEqualTo(2L);
        assertThat(dto2.bookerId()).isEqualTo(3L);
    }

    @Test
    void commentMapper_returnsNullForNullAndHandlesAuthorNullness() {
        assertThat(commentMapper.mapCommentCreateDtoToComment(null)).isNull();
        assertThat(commentMapper.mapCommentToCommentDto(null)).isNull();

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Nice");

        assertThat(commentMapper.mapCommentToCommentDto(comment).authorName()).isNull();

        User author = new User();
        author.setName("Bob");
        comment.setAuthor(author);

        assertThat(commentMapper.mapCommentToCommentDto(comment).authorName()).isEqualTo("Bob");
    }

    @Test
    void itemRequestMapper_returnsNullForNullInputs() {
        assertThat(itemRequestMapper.mapItemRequestToItemRequestDto(null)).isNull();
        assertThat(itemRequestMapper.mapItemRequestCreateDtoToItem(null)).isNull();
        assertThat(itemRequestMapper.mapItemRequestToItemRequestWithResponsesDto(null, null)).isNull();
    }

    @Test
    void itemRequestMapper_handlesAllRequestorAndItemsCombinations() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("d");

        assertThat(itemRequestMapper.mapItemRequestToItemRequestDto(request).requestorId()).isNull();

        User requestor = new User();
        requestor.setId(5L);
        request.setRequestor(requestor);

        assertThat(itemRequestMapper.mapItemRequestToItemRequestDto(request).requestorId()).isEqualTo(5L);

        var withItemsOnly = itemRequestMapper.mapItemRequestToItemRequestWithResponsesDto(null, java.util.List.of());
        assertThat(withItemsOnly).isNotNull();

        var withRequestOnly = itemRequestMapper.mapItemRequestToItemRequestWithResponsesDto(request, null);
        assertThat(withRequestOnly.id()).isEqualTo(1L);
        assertThat(withRequestOnly.items()).isEmpty();

        var withBoth = itemRequestMapper.mapItemRequestToItemRequestWithResponsesDto(request, java.util.List.of());
        assertThat(withBoth.id()).isEqualTo(1L);
        assertThat(withBoth.items()).isEmpty();
    }

    private BookingMapper createBookingMapper() {
        BookingMapper mapper = Mappers.getMapper(BookingMapper.class);
        try {
            var userField = mapper.getClass().getDeclaredField("userMapper");
            userField.setAccessible(true);
            userField.set(mapper, userMapper);

            var itemField = mapper.getClass().getDeclaredField("itemMapper");
            itemField.setAccessible(true);
            itemField.set(mapper, itemMapper);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return mapper;
    }
}
