package com.zor07.notesbackend.api.v1.mapper;

import com.zor07.notesbackend.api.v1.dto.NotebookDto;
import com.zor07.notesbackend.entity.Notebook;
import com.zor07.notesbackend.entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotebookMapper {

    NotebookDto toDto(final Notebook entity);

    @Mapping(target = "user", expression = "java(user)")
    Notebook toEntity(final NotebookDto dto, final @Context User user);

}
