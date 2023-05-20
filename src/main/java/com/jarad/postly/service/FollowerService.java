package com.jarad.postly.service;

public interface FollowerService {

    void addFollowerToAuthor(Long userId, Long authorId);

    void deleteFollowerFromAuthor(Long userId, Long authorId);
}
