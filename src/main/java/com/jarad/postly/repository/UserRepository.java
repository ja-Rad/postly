package com.jarad.postly.repository;

import com.jarad.postly.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String username);

    User findByEmail(String email);

    boolean existsByEmail(String email);


}