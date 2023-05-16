package com.jarad.postly.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "followers")
public class Follower implements Serializable {

    @EmbeddedId
    private FollowerId id;

    @Column(name = "creation_date")
    private Instant creationDate;

    @ManyToOne
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private Profile authorId;

    @ManyToOne
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private Profile profileId;

    @Embeddable
    @EqualsAndHashCode
    public static class FollowerId implements Serializable {

        @Column(name = "author_id")
        private Long authorId;

        @Column(name = "follower_id")
        private Long followerId;
    }
}
