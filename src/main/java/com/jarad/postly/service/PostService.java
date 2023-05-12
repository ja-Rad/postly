package com.jarad.postly.service;

import com.jarad.postly.entity.Post;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostService {
    List<Integer> returnListOfPageNumbers(int totalPages);

    Page<Post> returnPaginatedPostsByCreationDateDescending(int page, int size);
}
