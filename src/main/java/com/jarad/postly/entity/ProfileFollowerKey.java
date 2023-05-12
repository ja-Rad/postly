package com.jarad.postly.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ProfileFollowerKey implements Serializable {

    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "follower_id")
    private Long followerId;
}
