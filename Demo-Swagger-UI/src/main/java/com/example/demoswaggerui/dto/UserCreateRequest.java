package com.example.demoswaggerui.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "建立使用者的請求參數")
public class UserCreateRequest {

    @Schema(description = "使用者名稱", example = "zhangsan", minLength = 2, maxLength = 50)
    @NotBlank(message = "使用者名稱不可為空")
    @Size(min = 2, max = 50, message = "使用者名稱長度需介於 {min} 到 {max} 之間")
    private String username;

    @Schema(description = "電子郵件", example = "zhangsan@example.com")
    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "請提供正確的 Email 格式")
    private String email;

    @Schema(description = "生日", example = "1990-01-01", format = "date")
    @Past(message = "生日必須是過去的日期")
    private LocalDate birthday;

    public UserCreateRequest() {}

    public UserCreateRequest(String username, String email, LocalDate birthday) {
        this.username = username;
        this.email = email;
        this.birthday = birthday;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
}
