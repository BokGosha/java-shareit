package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.model.ItemRequest;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(source = "requestor.id", target = "requestorId")
    ItemRequestDto mapItemRequestToItemRequestDto(ItemRequest itemRequest);

    ItemRequest mapItemRequestCreateDtoToItem(ItemRequestCreateDto itemRequestCreateDto);

    ItemRequestWithResponsesDto mapItemRequestToItemRequestWithResponsesDto(ItemRequest itemRequest);
}
