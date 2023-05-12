package com.jarad.postly.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "profiles_followers", uniqueConstraints = {
        @UniqueConstraint(name = "uc_profilesfollowers", columnNames = {"author_id", "follower_id"})
})
public class ProfileFollower {

    @EmbeddedId
    private ProfileFollowerKey profileFollowerKey;

    @ManyToOne
    @MapsId("profileId")
    @JoinColumn(name = "author_id")
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    private Follower follower;

    private Instant creation_date;
}
