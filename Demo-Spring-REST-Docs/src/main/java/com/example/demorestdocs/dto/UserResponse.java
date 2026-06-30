package com.example.demorestdocs.dto;

import java.time.LocalDate;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
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
