package com.example.demorestdocs.controller;

import com.example.demorestdocs.dto.UserCreateRequest;
import com.example.demorestdocs.dto.UserResponse;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final Map<Long, UserResponse> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

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

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {

        log.info("查詢使用者 - id: {}", id);

        UserResponse response = store.get(id);
        if (response == null) {
            log.warn("使用者不存在 - id: {}", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}
