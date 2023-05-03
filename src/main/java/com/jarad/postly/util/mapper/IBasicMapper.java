package com.jarad.postly.util.mapper;

public interface IBasicMapper<EntityType, DtoType> {
    EntityType mapToEntity(DtoType dtoType);

    DtoType mapToDto(EntityType entityType);
}
