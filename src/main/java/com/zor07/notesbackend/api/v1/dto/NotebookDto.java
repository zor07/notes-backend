package com.zor07.notesbackend.api.v1.dto;

public record NotebookDto(
        Long id,
        String name,
        String description
) {}
