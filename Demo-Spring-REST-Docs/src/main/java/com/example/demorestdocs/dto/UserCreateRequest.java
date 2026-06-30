package com.example.demorestdocs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class UserCreateRequest {

    @NotBlank(message = "使用者名稱不可為空")
    @Size(min = 2, max = 50, message = "使用者名稱長度需介於 {min} 到 {max} 之間")
    private String username;

    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "請提供正確的 Email 格式")
    private String email;

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
