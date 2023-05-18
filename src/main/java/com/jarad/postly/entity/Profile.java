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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "profiles")
public class Profile implements Serializable {
    @Id
    @Column(name = "profile_id")
    private Long id;

    @NotNull
    @Column(name = "username")
    private String username;

    @NotNull
    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @OneToMany(mappedBy = "profile", orphanRemoval = true)
    private List<Post> posts;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "profile_id")
    private User user;

    @OneToMany(mappedBy = "authorId", orphanRemoval = true)
    private List<Follower> authors;

    @OneToMany(mappedBy = "followerId", orphanRemoval = true)
    private List<Follower> followers;

    @OneToMany(mappedBy = "profile", orphanRemoval = true)
    private List<Comment> comments;
}