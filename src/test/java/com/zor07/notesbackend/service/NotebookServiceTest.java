package com.zor07.notesbackend.service;


import com.zor07.notesbackend.entity.Notebook;
import com.zor07.notesbackend.entity.Role;
import com.zor07.notesbackend.entity.User;
import com.zor07.notesbackend.repository.NotebookRepository;
import com.zor07.notesbackend.repository.RoleRepository;
import com.zor07.notesbackend.repository.UserRepository;
import com.zor07.notesbackend.security.UserRole;
import com.zor07.notesbackend.spring.AbstractApplicationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class NotebookServiceTest extends AbstractApplicationTest {

    private static final String DEFAULT_PASSWORD = "pass";
    private static final String USER_1 = "user1";
    private static final String USER_2 = "user2";
    private static final String NOTEBOOK_NAME = "notebook name";
    private static final String NOTEBOOK_DESCRIPTION = "notebook desc";
    private static final String NOTEBOOK_DESCRIPTION_NEW = "notebook desc new";

    private @Autowired NotebookService notebookService;
    private @Autowired NotebookRepository notebookRepository;
    private @Autowired UserService userService;
    private @Autowired UserRepository userRepository;
    private @Autowired RoleRepository roleRepository;

    private User getUser(final String username) {
        return userService.getUser(username);
    }
    private User persistUser(final String name) {
        return userService.saveUser(new User(null, name, name, DEFAULT_PASSWORD, new ArrayList<>()));
    }

    private Role createRole() {
        return userService.saveRole(new Role(null, UserRole.ROLE_USER.getRoleName()));
    }

    private Notebook createNotebook(final User user) {
        final var notebook = new Notebook();
        notebook.setUser(user);
        notebook.setName(NOTEBOOK_NAME);
        notebook.setDescription(NOTEBOOK_DESCRIPTION);
        return notebookRepository.save(notebook);
    }

    private Notebook persistNotebook(final Notebook notebook) {
        return notebookRepository.save(notebook);
    }

    private void assertNotebook(final Notebook actual, final Notebook expected) {
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
    }

    private void clearDb() {
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
    void getNotebook_shouldReturnNotebook() {
        //given
        final var user = getUser(USER_1);
        final var expected = persistNotebook(createNotebook(user));
        //when
        final var actual = notebookService.getNotebook(expected.getId(), user.getId());
        //then
        assertNotebook(actual, expected);
    }

    @Test
    void getNotebooks_shouldReturnNotebooks() {
        //given
        final var user = getUser(USER_1);
        final var expected = persistNotebook(createNotebook(user));
        persistNotebook(createNotebook(user));
        persistNotebook(createNotebook(user));
        //when
        final var actual = notebookService.getNotebooks(user.getId());
        //then
        assertThat(actual).hasSize(3);
        actual.forEach(notebook -> assertNotebook(notebook, expected));
    }

    @Test
    void saveNotebook_shouldSaveNotebook() {
        //given
        final var user = getUser(USER_1);
        final var notebook = createNotebook(user);
        //when
        notebookService.saveNotebook(notebook);
        //then
        final var all = notebookRepository.findAll();
        final var actual = all.get(0);
        assertThat(all).hasSize(1);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUser().getUsername()).isEqualTo(USER_1);
        assertThat(actual.getName()).isEqualTo(NOTEBOOK_NAME);
        assertThat(actual.getDescription()).isEqualTo(NOTEBOOK_DESCRIPTION);
    }

    @Test
    void updateNotebook_shouldUpdateNotebook() {
        //given
        final var user = getUser(USER_1);
        final var notebook = persistNotebook(createNotebook(user));
        notebook.setDescription(NOTEBOOK_DESCRIPTION_NEW);

        //when
        notebookService.updateNotebook(notebook);
        //then
        final var all = notebookRepository.findAll();
        final var actual = all.get(0);
        assertThat(all).hasSize(1);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUser().getUsername()).isEqualTo(USER_1);
        assertThat(actual.getName()).isEqualTo(NOTEBOOK_NAME);
        assertThat(actual.getDescription()).isEqualTo(NOTEBOOK_DESCRIPTION_NEW);
    }

    @Test
    void deleteNotebook_shouldDeleteNotebook() {
        //given
        final var user = getUser(USER_1);
        final var notebook = persistNotebook(createNotebook(user));

        //when
        notebookService.deleteNotebook(notebook.getId(), user.getId());

        //then
        assertThat(notebookRepository.findAll()).isEmpty();
    }

    @Test
    void deleteNotebook_shouldNoteDeleteNotebookWhenUserIsWrong() {
        //given
        final var user1 = getUser(USER_1);
        final var user2 = getUser(USER_2);
        final var notebook = persistNotebook(createNotebook(user1));

        //when
        notebookService.deleteNotebook(notebook.getId(), user2.getId());

        //then
        assertThat(notebookRepository.findAll()).hasSize(1);
    }

}
