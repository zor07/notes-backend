package com.zor07.notesbackend.api.v1.mapper;

import com.zor07.notesbackend.api.v1.dto.NotebookDto;
import com.zor07.notesbackend.entity.Notebook;
import com.zor07.notesbackend.entity.User;
import org.mapstruct.factory.Mappers;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class NotebookMapperTest {

    private static final Long ID = 555L;
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String NAME = "NAME";
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final User USER = new User(null, USERNAME, USERNAME, PASSWORD, Collections.emptyList());

    private final NotebookMapper notebookMapper = Mappers.getMapper(NotebookMapper.class);


    @Test
    void shouldMapDtoToEntity() {
        //given
        final var dto = new NotebookDto(ID, NAME, DESCRIPTION);

        //when
        final var entity = notebookMapper.toEntity(dto, USER);

        //then
        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getName()).isEqualTo(NAME);
        assertThat(entity.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(entity.getUser().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void shouldMapEntityToDto() {
        //given
        final var entity = new Notebook();
        entity.setId(ID);
        entity.setName(NAME);
        entity.setDescription(DESCRIPTION);
        entity.setUser(USER);

        //when
        final var dto = notebookMapper.toDto(entity);

        //then
        assertThat(dto.id()).isEqualTo(ID);
        assertThat(dto.name()).isEqualTo(NAME);
        assertThat(dto.description()).isEqualTo(DESCRIPTION);
    }
}
