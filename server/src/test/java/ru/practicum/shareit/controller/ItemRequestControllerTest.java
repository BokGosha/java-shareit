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
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private ItemRequestDto itemRequestDto;
    private ItemRequestWithResponsesDto withResponsesDto;

    @BeforeEach
    public void setUp() {
        LocalDateTime created = LocalDateTime.now().withNano(0);
        itemRequestDto = new ItemRequestDto(1L, "Need a drill", 1L, created);
        withResponsesDto = new ItemRequestWithResponsesDto(1L, "Need a drill", created, List.of());
    }

    @Test
    @DisplayName("GET /requests")
    public void getUserRequests_shouldReturnList() throws Exception {
        when(requestService.getUserRequests(1L)).thenReturn(List.of(withResponsesDto));

        mockMvc.perform(get("/requests").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Need a drill"))
                .andExpect(jsonPath("$[0].items").isArray());

        verify(requestService).getUserRequests(1L);
    }

    @Test
    @DisplayName("GET /requests, missing X-Sharer-User-Id header")
    public void getUserRequests_whenHeaderMissing_shouldReturn400() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/all")
    public void getUsersRequests_shouldReturnList() throws Exception {
        when(requestService.getUsersRequests(1L)).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Need a drill"))
                .andExpect(jsonPath("$[0].requestorId").value(1L));

        verify(requestService).getUsersRequests(1L);
    }

    @Test
    @DisplayName("POST /requests")
    public void createRequest_shouldReturnCreated() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto("Need a drill");
        when(requestService.createRequest(eq(1L), any(ItemRequestCreateDto.class))).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.requestorId").value(1L));

        verify(requestService).createRequest(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    @DisplayName("POST /requests, requestor not found")
    public void createRequest_whenRequestorNotFound_shouldReturn404() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto("Need a drill");
        when(requestService.createRequest(eq(999L), any(ItemRequestCreateDto.class)))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /requests/{requestId}")
    public void getRequestById_shouldReturnRequest() throws Exception {
        when(requestService.getRequestById(1L)).thenReturn(withResponsesDto);

        mockMvc.perform(get("/requests/{requestId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.items").isArray());

        verify(requestService).getRequestById(1L);
    }

    @Test
    @DisplayName("GET /requests/{requestId}, not found")
    public void getRequestById_whenNotFound_shouldReturn404() throws Exception {
        when(requestService.getRequestById(999L))
                .thenThrow(new NotFoundException("Запрос с id=999 не найден"));

        mockMvc.perform(get("/requests/{requestId}", 999L))
                .andExpect(status().isNotFound());
    }
}
