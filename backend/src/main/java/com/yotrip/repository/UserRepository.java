package com.yotrip.repository;

import com.yotrip.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMobile(String mobile);
    Optional<User> findByEmail(String email);
    Optional<User> findByNameIgnoreCase(String name);
    boolean existsByMobile(String mobile);
    boolean existsByEmail(String email);
}
