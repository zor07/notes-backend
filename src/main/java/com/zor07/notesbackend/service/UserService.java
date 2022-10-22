package com.zor07.notesbackend.service;

import com.zor07.notesbackend.entity.Role;
import com.zor07.notesbackend.entity.User;

import java.security.Principal;
import java.util.List;

public interface UserService {

  default User getUser(final Principal principal) {
    final var username = principal.getName();
    return getUser(username);
  }

  User saveUser(User user);
  Role saveRole(Role role);
  void addRoleToUser(String username, String roleName);
  User getUser(String username);
  List<User> getUsers();

}
