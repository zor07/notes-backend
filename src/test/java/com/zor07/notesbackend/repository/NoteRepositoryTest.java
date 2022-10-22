package com.zor07.notesbackend.repository;

import com.zor07.notesbackend.entity.Note;
import com.zor07.notesbackend.entity.Notebook;
import com.zor07.notesbackend.entity.Role;
import com.zor07.notesbackend.entity.User;
import com.zor07.notesbackend.security.UserRole;
import com.zor07.notesbackend.service.UserService;
import com.zor07.notesbackend.spring.AbstractApplicationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class NoteRepositoryTest extends AbstractApplicationTest {

  private static final String USERNAME = "user";
  private static final String PASSWORD = "pass";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String DATA = "{\"data\": \"value\"}";
  private static final String NEW_DATA = "{\"data\": \"new value\"}";
  private static final String TITLE = "title";

  @Autowired
  private NotebookRepository notebookRepository;
  @Autowired
  private NoteRepository noteRepository;
  @Autowired
  private UserService userService;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private UserRepository userRepository;

  private void clearDb() {
    noteRepository.deleteAll();
    notebookRepository.deleteAll();
    userRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @BeforeClass
  void setup() {
    clearDb();
    final var user = userService.saveUser(new User(null, USERNAME, USERNAME, PASSWORD, new ArrayList<>()));
    userService.saveRole(new Role(null, UserRole.ROLE_USER.getRoleName()));
    userService.addRoleToUser(USERNAME, UserRole.ROLE_USER.getRoleName());
    notebookRepository.save(new Notebook(null, user, NAME, DESCRIPTION));
  }
  @AfterClass
  void teardown() {
    clearDb();
  }

  @Test
  void testCrud() {
    noteRepository.deleteAll();
    final var all = noteRepository.findAll();
    assertThat(all).isEmpty();

    final var note = new Note();
    note.setNotebook(notebookRepository.findAll().get(0));
    note.setTitle(TITLE);
    note.setData(DATA);

    final var id = noteRepository.save(note).getId();
    final var inserted = noteRepository.findById(id).get();
    assertThat(inserted).isNotNull();
    assertThat(inserted.getData()).isEqualTo(DATA);
    assertThat(inserted.getTitle()).isEqualTo(TITLE);
    assertThat(inserted.getNotebook().getName()).isEqualTo(NAME);
    assertThat(inserted.getNotebook().getDescription()).isEqualTo(DESCRIPTION);

    inserted.setData(NEW_DATA);
    noteRepository.save(inserted);

    final var updated = noteRepository.findById(id).get();
    assertThat(updated.getData()).isEqualTo(NEW_DATA);

    noteRepository.delete(updated);

    assertThat(noteRepository.findById(id)).isEmpty();
  }

  @Test
  void findAllByNotebookIdTest() {
    noteRepository.deleteAll();
    final var notebook = notebookRepository.findAll().get(0);
    final var note = new Note(null, notebook, TITLE, DATA);
    final var note1 = new Note(null, notebook, TITLE, DATA);
    noteRepository.save(note);
    noteRepository.save(note1);
    final var allByNotebookId = noteRepository.findAllByNotebookId(notebook.getId());
    assertThat(allByNotebookId).hasSize(2);
    assertThat(allByNotebookId.get(0).getId()).isNotNull();
    assertThat(allByNotebookId.get(0).getTitle()).isEqualTo(TITLE);
    assertThat(allByNotebookId.get(1).getId()).isNotNull();
    assertThat(allByNotebookId.get(1).getTitle()).isEqualTo(TITLE);
  }
}
