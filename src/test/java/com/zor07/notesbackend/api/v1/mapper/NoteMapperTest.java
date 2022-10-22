package com.zor07.notesbackend.api.v1.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zor07.notesbackend.api.v1.dto.NoteDto;
import com.zor07.notesbackend.api.v1.dto.NotebookDto;
import com.zor07.notesbackend.entity.Note;
import com.zor07.notesbackend.entity.NoteIdAndTitle;
import com.zor07.notesbackend.entity.Notebook;
import com.zor07.notesbackend.entity.User;
import org.mapstruct.factory.Mappers;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class NoteMapperTest {

    private static final Long ID = 555L;
    private static final String NOTEBOOK_DESCRIPTION = "DESCRIPTION";
    private static final String NOTEBOOK_NAME = "NAME";
    private static final String NOTE_DATA = "{\"data\": \"data\"}";
    private static final String NOTE_TITLE = "NOTE_TITLE";
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final User USER = new User(null, USERNAME, USERNAME, PASSWORD, Collections.emptyList());

    private final NoteMapper noteMapper = Mappers.getMapper(NoteMapper.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    record IdAndTitle(Long id, String title) implements NoteIdAndTitle {

        @Override
        public Long getId() {
            return id();
        }

        @Override
        public String getTitle() {
            return title();
        }
    }

    @Test
    void shouldMapDtoToEntity() throws IOException {
        //given
        final var notebookDto = new NotebookDto(ID, NOTEBOOK_NAME, NOTEBOOK_DESCRIPTION);
        final var noteDto = new NoteDto(ID, NOTE_TITLE, notebookDto, objectMapper.readTree(NOTE_DATA));

        //when
        final var entity = noteMapper.toEntity(noteDto, USER);

        //then
        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getTitle()).isEqualTo(NOTE_TITLE);
        assertThat(objectMapper.readTree(entity.getData())).isEqualTo(objectMapper.readTree(NOTE_DATA));
        assertThat(entity.getNotebook().getId()).isEqualTo(ID);
        assertThat(entity.getNotebook().getName()).isEqualTo(NOTEBOOK_NAME);
        assertThat(entity.getNotebook().getDescription()).isEqualTo(NOTEBOOK_DESCRIPTION);
        assertThat(entity.getNotebook().getUser().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void shouldMapEntityToDto() throws JsonProcessingException {
        //given
        final var notebookEntity = new Notebook();
        notebookEntity.setId(ID);
        notebookEntity.setName(NOTEBOOK_NAME);
        notebookEntity.setDescription(NOTEBOOK_DESCRIPTION);
        notebookEntity.setUser(USER);
        final var noteEntity = new Note();
        noteEntity.setId(ID);
        noteEntity.setTitle(NOTE_TITLE);
        noteEntity.setNotebook(notebookEntity);
        noteEntity.setData(NOTE_DATA);

        //when
        final var dto = noteMapper.toDto(noteEntity);

        //then
        assertThat(dto.id()).isEqualTo(ID);
        assertThat(dto.title()).isEqualTo(NOTE_TITLE);
        assertThat(dto.data()).isEqualTo(objectMapper.readTree(NOTE_DATA));
        assertThat(dto.notebookDto().id()).isEqualTo(ID);
        assertThat(dto.notebookDto().name()).isEqualTo(NOTEBOOK_NAME);
        assertThat(dto.notebookDto().description()).isEqualTo(NOTEBOOK_DESCRIPTION);
    }

    @Test
    void NoteIdAndTitleToDto() {
        //given
        final var idAndTitle = new IdAndTitle(ID, NOTE_TITLE);

        //when
        final var noteDto = noteMapper.toDto(idAndTitle);

        //then
        assertThat(noteDto.id()).isEqualTo(ID);
        assertThat(noteDto.title()).isEqualTo(NOTE_TITLE);
        assertThat(noteDto.data()).isNull();
        assertThat(noteDto.notebookDto()).isNull();
    }
}
