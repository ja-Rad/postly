package com.jarad.postly.util.mapper;

public interface BasicMapper<EntityType, DtoType> {
    EntityType mapToEntity(DtoType dtoType);

    DtoType mapToDto(EntityType entityType);
}
