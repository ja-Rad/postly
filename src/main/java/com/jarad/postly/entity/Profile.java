package com.jarad.postly.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profiles")
public class Profile implements Serializable {
    @Id
    @Column(name = "profile_id")
    private Long id;

    @NotBlank(message = "Username may not be blank")
    @Size(min = 3, max = 255, message = "Username must be between 3 and 36 characters long")
    @Column(name = "username")
    private String username;

    @NotNull
    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "profile_id")
    private User user;

    @OneToMany(mappedBy = "profile", orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "profileAuthor", orphanRemoval = true)
    private List<Follower> authors;

    @OneToMany(mappedBy = "profileFollower", orphanRemoval = true)
    private List<Follower> followers;

    @OneToMany(mappedBy = "profile", orphanRemoval = true)
    private List<Comment> comments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Profile profile = (Profile) o;
        return getId() != null && Objects.equals(getId(), profile.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}