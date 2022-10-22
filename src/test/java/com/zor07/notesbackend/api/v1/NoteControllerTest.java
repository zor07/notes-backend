package com.zor07.notesbackend.api.v1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zor07.notesbackend.api.v1.dto.NoteDto;
import com.zor07.notesbackend.api.v1.dto.NotebookDto;
import com.zor07.notesbackend.entity.Note;
import com.zor07.notesbackend.entity.Notebook;
import com.zor07.notesbackend.repository.NoteRepository;
import com.zor07.notesbackend.repository.NotebookRepository;
import com.zor07.notesbackend.repository.RoleRepository;
import com.zor07.notesbackend.repository.UserRepository;
import com.zor07.notesbackend.spring.AbstractApiTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NoteControllerTest extends AbstractApiTest {

  private static final String USER_1 = "user1";
  private static final String USER_2 = "user2";
  private static final String NOTEBOOK_NAME = "NOTEBOOK_NAME";
  private static final String NOTEBOOK_DESCRIPTION = "NOTEBOOK_DESCRIPTION";
  private static final String NOTE_TITLE = "NOTE_TITLE";
  private static final String NOTE_DATA = "{\"data\":\"data\"}";
  private static final String ENDPOINT = "/api/v1/notebook/%d/note";
  @Autowired
  private NotebookRepository notebookRepository;
  @Autowired
  private NoteRepository noteRepository;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private RoleRepository roleRepository;

  private MockMvc mvc;

  private Notebook createNotebook(final String username) {
    final var user = userService.getUser(username);
    final var notebook = new Notebook(null, user, NOTEBOOK_NAME, NOTEBOOK_DESCRIPTION);
    return notebookRepository.save(notebook);
  }

  private Note createNote(final Notebook notebook) {
    final var note = new Note(null, notebook, NOTE_TITLE, NOTE_DATA);
    return noteRepository.save(note);
  }
  private void clearDb() {
    noteRepository.deleteAll();
    notebookRepository.deleteAll();
    userRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @BeforeMethod
  public void setup() {
    clearDb();
    userService.saveUser(createUser(USER_1));
    userService.saveUser(createUser(USER_2));
    userService.saveRole(createRole());
    userService.addRoleToUser(USER_1, DEFAULT_ROLE);
    userService.addRoleToUser(USER_2, DEFAULT_ROLE);
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @AfterClass
  void teardown() {
    clearDb();
  }

  @Test
  void getNotes_shouldReturnNotes() throws Exception {
    //given
    final var authHeader = getAuthHeader(mvc, USER_1);
    final var notebook = createNotebook(USER_1);
    createNote(notebook);
    createNote(notebook);
    final var endpoint = String.format("/api/v1/notebooks/%d/notes", notebook.getId());

    //when
    final var content = mvc.perform(get(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, authHeader))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    //then
    final var notes = objectMapper.readValue(content, new TypeReference<List<NoteDto>>(){});
    assertThat(notes).hasSize(2);
    assertThat(notes.get(0).id()).isNotNull();
    assertThat(notes.get(0).data().isNull()).isTrue();
    assertThat(notes.get(0).title()).isEqualTo(NOTE_TITLE);
    assertThat(notes.get(1).id()).isNotNull();
    assertThat(notes.get(1).data().isNull()).isTrue();
    assertThat(notes.get(1).title()).isEqualTo(NOTE_TITLE);
  }

  @Test
  void getNote_shouldReturnNote() throws Exception {
    //given
    final var authHeader = getAuthHeader(mvc, USER_1);
    final var notebook = createNotebook(USER_1);
    final var note = createNote(notebook);
    final var endpoint = String.format("/api/v1/notebooks/%d/notes/%d", notebook.getId(), note.getId());
    //when
    final var content = mvc.perform(get(endpoint)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, authHeader))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    //then
    final var response = objectMapper.readValue(content, NoteDto.class);
    assertThat(response.id()).isEqualTo(note.getId());
    assertThat(response.title()).isEqualTo(note.getTitle());
    assertThat(response.data().toString()).isEqualTo(note.getData());
  }

  @Test
  void createNote_shouldCreateNote() throws Exception {
    //given
    final var authHeader = getAuthHeader(mvc, USER_1);
    final var notebook = createNotebook(USER_1);
    final var notebookDto = new NotebookDto(notebook.getId(), null, null);
    final var noteRequestDto = new NoteDto(null, NOTE_TITLE, notebookDto, objectMapper.readTree(NOTE_DATA));

    final var endpoint = String.format("/api/v1/notebooks/%d/notes", notebook.getId());

    //when
    final var resultActions = mvc.perform(post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(noteRequestDto))
                    .header(HttpHeaders.AUTHORIZATION, authHeader));

    //then
    resultActions.andExpect(status().isCreated());
    final var noteResponse = noteRepository.findAll().get(0);
    assertThat(noteResponse.getTitle()).isEqualTo(noteRequestDto.title());
    assertThat(objectMapper.readTree(noteResponse.getData())).isEqualTo(noteRequestDto.data());
  }

  @Test
  void updateNote_shouldUpdateNote() throws Exception {
    //given
    final var authHeader = getAuthHeader(mvc, USER_1);
    final var notebook = createNotebook(USER_1);
    createNote(notebook);
    final var noteId = noteRepository.findAll().get(0).getId();
    final var newTitle = "new Title";
    final var newData = "{\"data\":\"new Data\"}";
    final var notebookDto = new NotebookDto(notebook.getId(), null, null);
    final var noteRequestDto = new NoteDto(noteId, newTitle, notebookDto, objectMapper.readTree(newData));
    final var endpoint = String.format("/api/v1/notebooks/%d/notes", notebook.getId());

    //when
    mvc.perform(put(endpoint)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(noteRequestDto))
        .header(HttpHeaders.AUTHORIZATION, authHeader))
        .andExpect(status().isAccepted());

    //then
    final var all = noteRepository.findAll();
    assertThat(all).hasSize(1);
    final var note = all.get(0);
    assertThat(note.getTitle()).isEqualTo(newTitle);
    assertThat(objectMapper.readTree(note.getData())).isEqualTo(objectMapper.readTree(newData));
  }


  @Test
  void deleteNote_shouldDeleteNote() throws Exception {
    //given
    final var authHeader = getAuthHeader(mvc, USER_1);
    final var notebook = createNotebook(USER_1);
    final var note = createNote(notebook);
    final var endpoint = String.format("/api/v1/notebooks/%d/notes/%d", notebook.getId(), note.getId());

    //when
    mvc.perform(delete(endpoint)
              .contentType(MediaType.APPLICATION_JSON)
              .header(HttpHeaders.AUTHORIZATION, authHeader))
        .andExpect(status().isNoContent());

    //then
    assertThat(noteRepository.findAll()).isEmpty();
  }

  @Test
  void deleteNote_whenNoteNotExists_shouldReturnBadRequest() throws Exception {
    //given
    final var authHeader = getAuthHeader(mvc, USER_1);
    final var endpoint = "/api/v1/notebooks/777/notes/777";

    // when
    final var result = mvc.perform(delete(endpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, authHeader));
    //then
    result.andExpect(status().isBadRequest());

  }

}