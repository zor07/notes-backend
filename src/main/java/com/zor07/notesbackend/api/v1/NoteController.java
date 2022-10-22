package com.zor07.notesbackend.api.v1;

import com.zor07.notesbackend.api.v1.dto.NoteDto;
import com.zor07.notesbackend.api.v1.mapper.NoteMapper;
import com.zor07.notesbackend.service.NoteService;
import com.zor07.notesbackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notebooks/{notebookId}/notes")
@Api( tags = "Notes" )
public class NoteController {

    private final UserService userService;
    private final NoteService noteService;
    private final NoteMapper noteMapper;
    public NoteController(final UserService userService,
                          final NoteService noteService,
                          final NoteMapper noteMapper) {
        this.userService = userService;
        this.noteService = noteService;
        this.noteMapper = noteMapper;
    }

    @GetMapping(path = "/{noteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get note by notebook id and note id", response = NoteDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved note"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    public ResponseEntity<NoteDto> getNote(final @ApiIgnore Principal principal,
                                           final @PathVariable Long notebookId,
                                           final @PathVariable Long noteId) {
        final var userId = userService.getUser(principal).getId();
        return ResponseEntity.ok(noteMapper.toDto(noteService.getNote(notebookId, noteId, userId)));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get notes by notebook id", response = NoteDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved notes"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    public ResponseEntity<List<NoteDto>> getNotesByBook(final @ApiIgnore Principal principal,
                                                        final @PathVariable Long notebookId) {
        final var userId = userService.getUser(principal).getId();
        final var notes = noteService.getNotes(notebookId, userId)
                .stream()
                .map(noteMapper::toDto)
                .toList();
        return ResponseEntity.ok(notes);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create new note", response = NoteDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created note"),
            @ApiResponse(code = 401, message = "You are not authorized to create the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    public ResponseEntity<NoteDto> createNote(final @ApiIgnore Principal principal,
                                              final @PathVariable Long notebookId,
                                              final @RequestBody NoteDto dto) {

        final var user = userService.getUser(principal);
        final var note = noteService.saveNote(noteMapper.toEntity(dto, user));
        final var uri = URI.create(ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(String.format("/api/v1/notebooks/%d/notes/%d", note.getNotebook().getId(), note.getId()))
                .toUriString());
        return ResponseEntity.created(uri).body(noteMapper.toDto(note));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update note", response = NoteDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Successfully updated note"),
            @ApiResponse(code = 401, message = "You are not authorized to update the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    public ResponseEntity<NoteDto> updateNote(final @ApiIgnore Principal principal,
                                              final @PathVariable Long notebookId,
                                              final @RequestBody NoteDto dto) {
        final var user = userService.getUser(principal);
        final var note = noteService.updateNote(noteMapper.toEntity(dto, user));
        return ResponseEntity.accepted().body(noteMapper.toDto(note));
    }

    @DeleteMapping("/{noteId}")
    @ApiOperation(value = "Delete note by notebook id and note id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted note"),
            @ApiResponse(code = 401, message = "You are not authorized to update the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    public ResponseEntity<Void> deleteNote(final @ApiIgnore Principal principal,
                                           final @PathVariable Long notebookId,
                                           final @PathVariable Long noteId) {
        final var user = userService.getUser(principal);
        noteService.deleteNote(notebookId, noteId, user.getId());
        return ResponseEntity.noContent().build();
    }

}
