package com.zor07.notesbackend.service;


import com.zor07.notesbackend.entity.Note;
import com.zor07.notesbackend.entity.Notebook;
import com.zor07.notesbackend.entity.Role;
import com.zor07.notesbackend.entity.User;
import com.zor07.notesbackend.exception.IllegalResourceAccessException;
import com.zor07.notesbackend.repository.NoteRepository;
import com.zor07.notesbackend.repository.NotebookRepository;
import com.zor07.notesbackend.repository.RoleRepository;
import com.zor07.notesbackend.repository.UserRepository;
import com.zor07.notesbackend.security.UserRole;
import com.zor07.notesbackend.spring.AbstractApplicationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class NoteServiceTest extends AbstractApplicationTest {

    private static final String DEFAULT_PASSWORD = "pass";
    private static final String USER_1 = "user1";
    private static final String USER_2 = "user2";
    private static final String NOTEBOOK_NAME = "notebook name";
    private static final String NOTEBOOK_DESCRIPTION = "notebook desc";

    private static final String NOTE_DATA = "{\"data\":\"value\"}";
    private static final String NOTE_TITLE = "note title";
    private static final String NOTE_TITLE_NEW = "new note title";
    private static final String NOTE_DATA_NEW = "{\"data\":\"new data\"}";
    private static final String NOTE_DATA_WRONG_JSON = "some data";
    private @Autowired NoteService noteService;
    private @Autowired NoteRepository noteRepository;
    private @Autowired NotebookRepository notebookRepository;
    private @Autowired UserService userService;
    private @Autowired UserRepository userRepository;
    private @Autowired RoleRepository roleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User getUser(final String username) {
        return userService.getUser(username);
    }
    private User persistUser(final String name) {
        return userService.saveUser(new User(null, name, name, DEFAULT_PASSWORD, new ArrayList<>()));
    }

    private Role createRole() {
        return userService.saveRole(new Role(null, UserRole.ROLE_USER.getRoleName()));
    }

    private Note createNote(final Notebook notebook) {
        final var note = new Note();
        note.setTitle(NOTE_TITLE);
        note.setData(NOTE_DATA);
        note.setNotebook(notebook);
        return note;
    }

    private Notebook createNotebook(final User user) {
        final var notebook = new Notebook();
        notebook.setUser(user);
        notebook.setName(NOTEBOOK_NAME);
        notebook.setDescription(NOTEBOOK_DESCRIPTION);
        return notebookRepository.save(notebook);
    }

    private Note persistNote(final Note note) {
        return noteRepository.save(note);
    }

    private Notebook persistNotebook(final Notebook notebook) {
        return notebookRepository.save(notebook);
    }

    private void assertNote(final Note actual, final Note expected) throws IOException {
        assertThat(expected.getId()).isNotNull();
        assertThat(expected.getTitle()).isEqualTo(actual.getTitle());
        assertThat(objectMapper.readTree(expected.getData())).isEqualTo(objectMapper.readTree(expected.getData()));
        assertThat(expected.getNotebook().getId()).isNotNull();
        assertThat(expected.getNotebook().getName()).isEqualTo(actual.getNotebook().getName());
        assertThat(expected.getNotebook().getDescription()).isEqualTo(actual.getNotebook().getDescription());
    }

    private String getAllExceptionMessages(final String messages, final Throwable e) {
        if (e.getCause() == null) {
            return messages + " " + e.getMessage();
        }
        return getAllExceptionMessages(messages + " " + e.getMessage(), e.getCause());
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
        userService.saveUser(persistUser(USER_1));
        userService.saveUser(persistUser(USER_2));
        userService.saveRole(createRole());
        userService.addRoleToUser(USER_1, UserRole.ROLE_USER.getRoleName());
        userService.addRoleToUser(USER_2, UserRole.ROLE_USER.getRoleName());
    }

    @AfterClass
    void teardown() {
        clearDb();
    }

    @Test
    void getNote_shouldReturnNote() throws Exception {
        //given
        final var user = getUser(USER_1);
        final var notebook = persistNotebook(createNotebook(user));
        final var persistedNote = persistNote(createNote(notebook));
        //when
        final var note = noteService.getNote(notebook.getId(), persistedNote.getId(), user.getId());
        //then
        assertNote(note, persistedNote);
    }

    @Test (expectedExceptions = IllegalResourceAccessException.class)
    void getNote_shouldThrowExceptionWhenUserIsWrong() throws Exception {
        //given
        final var user1 = getUser(USER_1);
        final var user2 = getUser(USER_2);
        final var notebook = persistNotebook(createNotebook(user1));
        final var persistedNote = persistNote(createNote(notebook));
        //when
        noteService.getNote(notebook.getId(), persistedNote.getId(), user2.getId());
        //then exception
    }

    @Test
    void getNotes_shouldReturnNotes() {
        //given
        final var user = getUser(USER_1);
        final var notebook = persistNotebook(createNotebook(user));
        persistNote(createNote(notebook));
        persistNote(createNote(notebook));
        persistNote(createNote(notebook));
        //when
        final var actualNotes = noteService.getNotes(notebook.getId(), user.getId());

        //then
        assertThat(actualNotes).hasSize(3);
        assertThat(actualNotes).allMatch(noteIdAndTitle -> noteIdAndTitle.getTitle().equals(NOTE_TITLE) && noteIdAndTitle.getId() != null);
    }

    @Test (expectedExceptions = IllegalResourceAccessException.class)
    void getNotes_shouldThrowExceptionWhenUserIsWrong() throws Exception {
        //given
        final var user1 = getUser(USER_1);
        final var user2 = getUser(USER_2);
        final var notebook = persistNotebook(createNotebook(user1));
        persistNote(createNote(notebook));
        //when
        noteService.getNotes(notebook.getId(), user2.getId());
        //then exception
    }

    @Test
    void saveNote_shouldCreateNote() throws IOException {
        //given
        final var user = getUser(USER_1);
        final var notebook = persistNotebook(createNotebook(user));
        final var note = createNote(notebook);

        //when
        noteService.saveNote(note);

        //then
        final var notes = noteRepository.findAll();
        assertThat(notes).hasSize(1);
        assertNote(notes.get(0), note);
    }

    @Test(expectedExceptions = IllegalResourceAccessException.class)
    void saveNote_shouldThrowExceptionWhenUserIsWrong() throws IOException {
        //given
        final var user1 = getUser(USER_1);
        final var user2 = getUser(USER_2);
        final var notebook = persistNotebook(createNotebook(user1));
        notebook.setUser(user2);
        final var note = createNote(notebook);

        //when
        noteService.saveNote(note);

        //then
        //should throw exception
    }

    @Test
    void saveNote_shouldThrowExceptionWhenNoteDataIsNotValidJson() {
        //given
        final var user1 = getUser(USER_1);
        final var notebook = persistNotebook(createNotebook(user1));
        final var note = createNote(notebook);
        note.setData(NOTE_DATA_WRONG_JSON);

        //when
        try {
            noteService.saveNote(note);
        } catch (Exception e) {
        //then
            final var allExceptionMessages = getAllExceptionMessages("", e);
            assertThat(allExceptionMessages).contains("The String is not in JSON format");
        }

    }

    @Test
    void updateNote_shouldUpdateNote() throws IOException {
        //given
        final var user = getUser(USER_1);
        final var notebook = persistNotebook(createNotebook(user));
        final var note = persistNote(createNote(notebook));
        note.setTitle(NOTE_TITLE_NEW);
        note.setData(NOTE_DATA_NEW);

        //when
        noteService.updateNote(note);

        //then
        final var all = noteRepository.findAll();
        assertThat(all).hasSize(1);
        assertNote(all.get(0), note);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void updateNote_shouldThrowExceptionWhenNoteIdIsNull() throws IOException {
        //given
        final var user1 = getUser(USER_1);
        final var user2 = getUser(USER_2);
        final var notebook = persistNotebook(createNotebook(user1));
        notebook.setUser(user2);
        final var note = createNote(notebook);

        //when
        noteService.updateNote(note);

        //then
        //should throw exception
    }

    @Test(expectedExceptions = IllegalResourceAccessException.class)
    void updateNote_shouldThrowExceptionWhenUserIsWrong() throws IOException {
        //given
        final var user1 = getUser(USER_1);
        final var user2 = getUser(USER_2);
        final var notebook = persistNotebook(createNotebook(user1));
        notebook.setUser(user2);
        final var note = persistNote(createNote(notebook));
        note.setTitle(NOTE_TITLE_NEW);

        //when
        noteService.updateNote(note);

        //then
        //should throw exception
    }

    @Test
    void updateNote_shouldThrowExceptionWhenNoteDataIsNotValidJson() {
        //given
        final var user = getUser(USER_1);
        final var notebook = persistNotebook(createNotebook(user));
        final var note = persistNote(createNote(notebook));
        note.setData(NOTE_DATA_WRONG_JSON);

        //when
        try {
            noteService.updateNote(note);
        } catch (Exception e) {
            //then
            final var allExceptionMessages = getAllExceptionMessages("", e);
            assertThat(allExceptionMessages).contains("The String is not in JSON format");
        }
    }

    @Test
    void deleteNote_shouldDeleteNote() {
        //given
        final var user = getUser(USER_1);
        final var notebook = persistNotebook(createNotebook(user));
        final var note = persistNote(createNote(notebook));

        //when
        noteService.deleteNote(notebook.getId(), note.getId(), user.getId());

        //then
        assertThat(noteRepository.findAll()).isEmpty();
    }

    @Test(expectedExceptions = IllegalResourceAccessException.class)
    void deleteNote_shouldThrowExceptionWhenUserIsWrong() {
        //given
        final var user1 = getUser(USER_1);
        final var user2 = getUser(USER_2);
        final var notebook = persistNotebook(createNotebook(user1));
        final var note = persistNote(createNote(notebook));

        //when
        noteService.deleteNote(notebook.getId(), note.getId(), user2.getId());

        //then
        //should throw exception
    }
}
