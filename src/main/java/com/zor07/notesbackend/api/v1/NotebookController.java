package com.zor07.notesbackend.api.v1;

import com.zor07.notesbackend.api.v1.dto.NotebookDto;
import com.zor07.notesbackend.api.v1.mapper.NotebookMapper;
import com.zor07.notesbackend.service.NotebookService;
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
@RequestMapping("/api/v1/notebooks")
@Api(tags = "Notebooks")
public class NotebookController {

    private final NotebookService notebookService;
    private final UserService userService;
    private final NotebookMapper notebookMapper;

    public NotebookController(final NotebookService notebookService,
                              final UserService userService,
                              final NotebookMapper notebookMapper) {
        this.notebookService = notebookService;
        this.userService = userService;
        this.notebookMapper = notebookMapper;
    }

    @GetMapping(path = "/{notebookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get notebook by id", response = NotebookDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved notebook"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    public ResponseEntity<NotebookDto> getNotebook(final @ApiIgnore Principal principal,
                                                   final @PathVariable Long notebookId) {
        final var user = userService.getUser(principal);
        final var notebook = notebookService.getNotebook(notebookId, user.getId());
        return ResponseEntity.ok().body(notebookMapper.toDto(notebook));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get notebooks of current user", response = NotebookDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved notebooks"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    public List<NotebookDto> getNotebooks(final @ApiIgnore Principal principal) {
        final var user = userService.getUser(principal);
        return notebookService.getNotebooks(user.getId())
                .stream()
                .map(notebookMapper::toDto)
                .toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create new notebook", response = NotebookDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created notebook"),
            @ApiResponse(code = 401, message = "You are not authorized to create the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    public ResponseEntity<NotebookDto> createNotebook(final @ApiIgnore Principal principal,
                                                      final @RequestBody NotebookDto dto) {
        final var user = userService.getUser(principal);
        final var saved = notebookService.saveNotebook(notebookMapper.toEntity(dto, user));
        final var uri = URI.create(ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(String.format("/api/v1/notebooks/%d", saved.getId()))
                .toUriString());
        return ResponseEntity.created(uri).body(notebookMapper.toDto(saved));
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update notebook", response = NotebookDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Successfully created notebook"),
            @ApiResponse(code = 400, message = "Payload contains illegal data"),
            @ApiResponse(code = 401, message = "You are not authorized to update the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    public ResponseEntity<NotebookDto> updateNotebook(final @ApiIgnore Principal principal,
                                                      final @RequestBody NotebookDto notebook) {
        final var user = userService.getUser(principal);
        final var updated = notebookService.updateNotebook(notebookMapper.toEntity(notebook, user));
        return ResponseEntity.accepted().body(notebookMapper.toDto(updated));
    }

    @DeleteMapping("/{notebookId}")
    @ApiOperation(value = "Delete notebook")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted notebook"),
            @ApiResponse(code = 401, message = "You are not authorized to delete the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
    })
    public ResponseEntity<Void> deleteNotebook(final @ApiIgnore Principal principal,
                                               final @PathVariable Long notebookId) {
        final var user = userService.getUser(principal);
        notebookService.deleteNotebook(notebookId, user.getId());
        return ResponseEntity.noContent().build();
    }

}
