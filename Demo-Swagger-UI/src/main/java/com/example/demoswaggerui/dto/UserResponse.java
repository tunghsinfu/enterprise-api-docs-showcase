package com.example.demoswaggerui.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "使用者回傳資料")
public class UserResponse {

    @Schema(description = "使用者 ID", example = "1")
    private Long id;

    @Schema(description = "使用者名稱", example = "zhangsan")
    private String username;

    @Schema(description = "電子郵件", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "生日", example = "1990-01-01")
    private LocalDate birthday;

    public UserResponse() {}

    public UserResponse(Long id, String username, String email, LocalDate birthday) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.birthday = birthday;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
}
