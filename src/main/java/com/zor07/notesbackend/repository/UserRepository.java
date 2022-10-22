package com.zor07.notesbackend.repository;

import com.zor07.notesbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  User findByUsername(String username);

}
