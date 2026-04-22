package ru.practicum.shareit.item.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMoreDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "isAvailable", target = "available")
    ItemDto mapItemToItemDto(Item item);

    @Mapping(source = "isAvailable", target = "available")
    ItemMoreDto mapItemToItemMoreDto(Item item);

    @Mapping(source = "available", target = "isAvailable")
    Item mapItemCreateDtoToItem(ItemCreateDto itemCreateDto);

    ItemShortDto mapItemToItemShortDto(Item item);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "available", target = "isAvailable")
    void updateItemFields(@MappingTarget Item item, ItemUpdateDto itemUpdateDto);
}
