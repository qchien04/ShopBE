package com.repository;

import com.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepo extends JpaRepository<User, Integer> {

    @Query("""
                select distinct u
                from User u
                left join fetch u.userRoles ur
                left join fetch ur.role
                where u.username = :username
            """)
    Optional<User> findByUsername(@Param("username") String username);

    @Query("""
                select u
                from User u
                where u.id = :id
            """)
    Optional<User> findByUserIdLong(@Param("id") Long id);

    @Query("""
                select distinct u
                from User u
                left join fetch u.userRoles ur
                left join fetch ur.role
                where u.id = :id
            """)
    Optional<User> findByIdWithRole(@Param("id") Long id);

    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.username like %:query% or u.email like %:query%")
    List<User> searchUser(@Param("query") String query);

    @Query("""
                UPDATE User u
                SET u.isActive = true
                WHERE u.email = :email
            """)
    @Modifying
    @Transactional
    int activateUserByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :from")
    Long countNewUsers(@Param("from") LocalDateTime from);

    @Query("SELECT u FROM User u WHERE u.createdAt >= :from ORDER BY u.createdAt DESC")
    List<User> findNewUsers(@Param("from") LocalDateTime from);

    @Query("SELECT distinct u FROM User u left join fetch u.userRoles ur left join fetch ur.role ORDER BY u.createdAt DESC")
    List<User> findAllNewUsers();

}
