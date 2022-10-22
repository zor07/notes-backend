package com.zor07.notesbackend.repository;

import com.zor07.notesbackend.entity.Notebook;
import com.zor07.notesbackend.entity.Role;
import com.zor07.notesbackend.entity.User;
import com.zor07.notesbackend.security.UserRole;
import com.zor07.notesbackend.service.UserService;
import com.zor07.notesbackend.spring.AbstractApplicationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class NotebookRepositoryTest extends AbstractApplicationTest {

  private static final String USERNAME = "user";
  private static final String PASSWORD = "pass";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String NEW_DESCRIPTION = "new description";

  @Autowired
  private NotebookRepository notebookRepository;
  @Autowired
  private UserService userService;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private UserRepository userRepository;

  private void clearDb() {
    notebookRepository.deleteAll();
    userRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @BeforeClass
  void setup() {
    clearDb();
    userService.saveUser(new User(null, USERNAME, USERNAME, PASSWORD, new ArrayList<>()));
    userService.saveRole(new Role(null, UserRole.ROLE_USER.getRoleName()));
    userService.addRoleToUser(USERNAME, UserRole.ROLE_USER.getRoleName());
  }

  @Test
  void testCrud() {

    notebookRepository.deleteAll();
    final var all = notebookRepository.findAll();
    assertThat(all).isEmpty();

    final var notebook = new Notebook();
    notebook.setUser(userService.getUser(USERNAME));
    notebook.setName(NAME);
    notebook.setDescription(DESCRIPTION);

    final var id = notebookRepository.save(notebook).getId();
    final var inserted = notebookRepository.findById(id).get();
    assertThat(inserted).isNotNull();
    assertThat(inserted.getName()).isEqualTo(NAME);
    assertThat(inserted.getDescription()).isEqualTo(DESCRIPTION);

    inserted.setDescription(NEW_DESCRIPTION);
    notebookRepository.save(inserted);

    final var updated = notebookRepository.findById(id).get();
    assertThat(updated.getDescription()).isEqualTo(NEW_DESCRIPTION);

    notebookRepository.delete(updated);

    assertThat(notebookRepository.findById(id)).isEmpty();
  }

  @Test
  void findAllByUserIdTest() {
    notebookRepository.deleteAll();
    final var user = userService.getUser(USERNAME);
    final var notebook = new Notebook(null, user, NAME, DESCRIPTION);
    final var notebook1 = new Notebook(null, user, NAME, DESCRIPTION);
    notebookRepository.save(notebook);
    notebookRepository.save(notebook1);
    final var allByUserId = notebookRepository.findAllByUserId(user.getId());
    assertThat(allByUserId).hasSize(2);
    assertThat(allByUserId.get(0).getId()).isNotNull();
    assertThat(allByUserId.get(0).getName()).isEqualTo(NAME);
    assertThat(allByUserId.get(0).getDescription()).isEqualTo(DESCRIPTION);
    assertThat(allByUserId.get(1).getId()).isNotNull();
    assertThat(allByUserId.get(1).getName()).isEqualTo(NAME);
    assertThat(allByUserId.get(1).getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  void findByIdAndUserIdTest() {
    notebookRepository.deleteAll();
    final var user = userService.getUser(USERNAME);
    final var notebook = new Notebook(null, user, NAME, DESCRIPTION);

    final var id = notebookRepository.save(notebook).getId();
    final var byIdAndUserId = notebookRepository.findByIdAndUserId(id, user.getId());
    assertThat(byIdAndUserId.getName()).isEqualTo(NAME);
    assertThat(byIdAndUserId.getDescription()).isEqualTo(DESCRIPTION);
  }
}
