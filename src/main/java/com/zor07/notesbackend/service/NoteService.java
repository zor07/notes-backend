package com.zor07.notesbackend.service;

import com.zor07.notesbackend.entity.Note;
import com.zor07.notesbackend.entity.NoteIdAndTitle;
import com.zor07.notesbackend.exception.IllegalResourceAccessException;
import com.zor07.notesbackend.repository.NoteRepository;
import com.zor07.notesbackend.repository.NotebookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;

@Service
@Transactional
public class NoteService {
    private final NoteRepository noteRepository;
    private final NotebookRepository notebookRepository;

    public NoteService(final NoteRepository noteRepository,
                       final NotebookRepository notebookRepository) {
        this.noteRepository = noteRepository;
        this.notebookRepository = notebookRepository;
    }

    public Note getNote(final Long notebookId, final Long noteId, final Long userId) {
        if (notUsersNotebook(userId, notebookId)) {
            throw new IllegalResourceAccessException();
        }
        return noteRepository.getById(noteId);
    }

    public List<NoteIdAndTitle> getNotes(final Long notebookId, final Long userId) {
        if (notUsersNotebook(userId, notebookId)) {
            throw new IllegalResourceAccessException();
        }
        return noteRepository.findAllByNotebookId(notebookId);
    }

    public Note saveNote(final @Valid Note note) {
        final var userId = note.getNotebook().getUser().getId();
        final var notebookId = note.getNotebook().getId();
        if (notUsersNotebook(userId, notebookId)) {
            throw new IllegalResourceAccessException();
        }
        return noteRepository.save(note);
    }

    public Note updateNote(final @Valid Note note) {
        if (note.getId() == null) {
            throw new IllegalArgumentException();
        }
        return saveNote(note);
    }

    public void deleteNote(final Long notebookId, final Long noteId, final Long userId) {
        if (notUsersNotebook(userId, notebookId)) {
            throw new IllegalResourceAccessException();
        }
        noteRepository.deleteAllByIdAndNotebookId(noteId, notebookId);
    }

    private boolean notUsersNotebook(final Long userId, final Long notebookId) {
        return notebookRepository.findByIdAndUserId(notebookId, userId) == null;
    }
}
