package com.chess.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.chess.model.entity.Game.GameStatus;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "AUTH_USER")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "S_ID", nullable = false, unique = true)
    private String id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "S_USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @NotNull
    @Email(message = "Email should be valid")
    @Column(name = "S_EMAIL", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "S_PASSWORD", nullable = false)
    @JsonIgnore //Prevents the hashed password from being serialized and sent in the JSON response. Will use later also json ignore in the getter so that serialization is not affected. But use jsonproperty in the setter to enable the deserialization when user sends the json.
    private String password;

    @Column(name = "D_CREATEDAT", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "D_LASTLOGIN")
    private LocalDateTime lastLogin = LocalDateTime.now();

    //Won't be called for updates (that would use @PreUpdate instead)
    @PrePersist//Called by JPA/Hibernate automatically//This annotation is used to specify that the method should be called when the entity is persisted to the database. It captures the exact database insertion time.
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastLogin = LocalDateTime.now();
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(final String password) {
        this.password = password;
    }
    @Builder
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
} 