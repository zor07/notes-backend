package com.zor07.notesbackend.repository;

import com.zor07.notesbackend.entity.Note;
import com.zor07.notesbackend.entity.NoteIdAndTitle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
  List<NoteIdAndTitle> findAllByNotebookId(Long notebookId);

  void deleteAllByIdAndNotebookId(Long id, Long notebookId);
}
