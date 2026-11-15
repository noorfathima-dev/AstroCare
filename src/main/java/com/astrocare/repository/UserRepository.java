package com.astrocare.repository;

import com.astrocare.entity.User;
import com.astrocare.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    /** All users holding a role, e.g. every flight surgeon to notify on an alert. */
    @Query("select u from User u join u.roles r where r.name = :role and u.enabled = true")
    List<User> findAllEnabledByRole(@Param("role") RoleName role);
}
