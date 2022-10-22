package com.zor07.notesbackend.service;

import com.zor07.notesbackend.entity.Notebook;
import com.zor07.notesbackend.repository.NotebookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotebookService {

    private final NotebookRepository repository;

    public NotebookService(NotebookRepository repository) {
        this.repository = repository;
    }

    public List<Notebook> getNotebooks(final Long userId) {
        return repository.findAllByUserId(userId);
    }

    public Notebook getNotebook(final Long notebookId, final Long userId) {
        return repository.findByIdAndUserId(notebookId, userId);
    }

    public Notebook saveNotebook(final Notebook notebook) {
        return repository.save(notebook);
    }

    public Notebook updateNotebook(final Notebook notebook) {
        if (notebook.getId() == null) {
            throw new IllegalArgumentException();
        }
        return saveNotebook(notebook);
    }

    public void deleteNotebook(final Long notebookId, final Long userId) {
        repository.deleteByIdAndUserId(notebookId, userId);
    }
}
