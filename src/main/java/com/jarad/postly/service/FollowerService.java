package com.jarad.postly.service;

public interface FollowerService {

    void addFollowerToAuthor(Long followerId, Long authorId);

    void deleteFollowerFromAuthor(Long followerId, Long authorId);
}
