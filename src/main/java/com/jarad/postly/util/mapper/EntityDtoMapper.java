package com.jarad.postly.util.mapper;

public interface EntityDtoMapper<EntityType, DtoType> {
    EntityType mapToEntity(DtoType dtoType);

    DtoType mapToDto(EntityType entityType);
}
