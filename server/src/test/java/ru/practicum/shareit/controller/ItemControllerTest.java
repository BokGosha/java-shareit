package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMoreDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private ItemMoreDto itemMoreDto;

    @BeforeEach
    public void setUp() {
        itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, List.of());
        itemMoreDto = new ItemMoreDto(1L, "Drill", "Powerful drill", true, null, null, List.of());
    }

    @Test
    @DisplayName("GET /items")
    public void getItems_shouldReturnList() throws Exception {
        ItemDto second = new ItemDto(2L, "Saw", "Hand saw", true, List.of());
        when(itemService.getItems()).thenReturn(List.of(itemDto, second));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Drill"))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(itemService).getItems();
    }

    @Test
    @DisplayName("GET /items/{itemId}, item exists")
    public void getItemById_whenExists_shouldReturnItem() throws Exception {
        when(itemService.getItemById(1L)).thenReturn(itemMoreDto);

        mockMvc.perform(get("/items/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Powerful drill"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService).getItemById(1L);
    }

    @Test
    @DisplayName("GET /items/{itemId}, item does not exist")
    public void getItemById_whenNotFound_shouldReturn404() throws Exception {
        when(itemService.getItemById(999L))
                .thenThrow(new NotFoundException("Вещь с id=999 не найдена"));

        mockMvc.perform(get("/items/{itemId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /items with X-Sharer-User-Id header")
    public void getItemsByOwnerId_shouldReturnList() throws Exception {
        when(itemService.getItemsByOwnerId(1L)).thenReturn(List.of(itemMoreDto));

        mockMvc.perform(get("/items").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemService).getItemsByOwnerId(1L);
    }

    @Test
    @DisplayName("GET /items/search")
    public void getItemsByText_shouldReturnList() throws Exception {
        when(itemService.getItemsByText("drill")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Drill"));

        verify(itemService).getItemsByText("drill");
    }

    @Test
    @DisplayName("POST /items")
    public void createItem_shouldReturn201() throws Exception {
        ItemCreateDto request = new ItemCreateDto("Drill", "Powerful drill", true, null);
        when(itemService.createItem(eq(1L), any(ItemCreateDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));

        verify(itemService).createItem(eq(1L), any(ItemCreateDto.class));
    }

    @Test
    @DisplayName("POST /items, missing X-Sharer-User-Id header")
    public void createItem_whenHeaderMissing_shouldReturn400() throws Exception {
        ItemCreateDto request = new ItemCreateDto("Drill", "Powerful drill", true, null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment")
    public void createComment_shouldReturn201() throws Exception {
        CommentCreateDto request = new CommentCreateDto("Nice item");
        CommentDto response = new CommentDto(1L, "Nice item", "John", LocalDateTime.now());
        when(itemService.createComment(eq(1L), eq(1L), any(CommentCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Nice item"))
                .andExpect(jsonPath("$.authorName").value("John"));

        verify(itemService).createComment(eq(1L), eq(1L), any(CommentCreateDto.class));
    }

    @Test
    @DisplayName("PATCH /items/{id}")
    public void updateItem_shouldReturnUpdated() throws Exception {
        ItemUpdateDto request = new ItemUpdateDto("Drill Updated", null, null);
        ItemDto updated = new ItemDto(1L, "Drill Updated", "Powerful drill", true, List.of());
        when(itemService.updateItem(eq(1L), eq(1L), any(ItemUpdateDto.class))).thenReturn(updated);

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill Updated"));

        verify(itemService).updateItem(eq(1L), eq(1L), any(ItemUpdateDto.class));
    }

    @Test
    @DisplayName("PATCH /items/{id}, user is not owner")
    public void updateItem_whenUserIsNotOwner_shouldReturn403() throws Exception {
        ItemUpdateDto request = new ItemUpdateDto("X", null, null);
        when(itemService.updateItem(eq(1L), eq(2L), any(ItemUpdateDto.class)))
                .thenThrow(new UserIsNotOwnerException("Пользователь не является владельцем"));

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header(USER_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /items/{id}, item not found")
    public void updateItem_whenItemNotFound_shouldReturn404() throws Exception {
        ItemUpdateDto request = new ItemUpdateDto("X", null, null);
        when(itemService.updateItem(eq(999L), eq(1L), any(ItemUpdateDto.class)))
                .thenThrow(new NotFoundException("Вещь с id=999 не найдена"));

        mockMvc.perform(patch("/items/{id}", 999L)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
