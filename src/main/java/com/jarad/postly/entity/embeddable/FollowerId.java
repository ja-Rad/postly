package com.jarad.postly.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class FollowerId implements Serializable {

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "follower_id")
    private Long followerId;
}
