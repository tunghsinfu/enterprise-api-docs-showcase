package com.example.demoswaggerui.controller;

import com.example.demoswaggerui.dto.UserCreateRequest;
import com.example.demoswaggerui.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "使用者管理", description = "使用者 CRUD 相關 API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final Map<Long, UserResponse> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    @Operation(summary = "建立使用者", description = "傳入使用者資訊並建立新使用者")
    @ApiResponse(responseCode = "201", description = "建立成功")
    @ApiResponse(responseCode = "400", description = "請求參數驗證失敗",
                 content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request) {

        log.info("收到建立使用者請求 - username: {}, email: {}, birthday: {}",
                request.getUsername(), request.getEmail(), request.getBirthday());

        long id = idSeq.getAndIncrement();
        UserResponse response = new UserResponse(
                id, request.getUsername(), request.getEmail(), request.getBirthday());
        store.put(id, response);

        log.info("使用者建立成功 - id: {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "取得使用者", description = "依 ID 取得使用者資訊")
    @ApiResponse(responseCode = "200", description = "成功")
    @ApiResponse(responseCode = "404", description = "使用者不存在")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "使用者 ID", example = "1")
            @PathVariable Long id) {

        log.info("查詢使用者 - id: {}", id);

        UserResponse response = store.get(id);
        if (response == null) {
            log.warn("使用者不存在 - id: {}", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}
