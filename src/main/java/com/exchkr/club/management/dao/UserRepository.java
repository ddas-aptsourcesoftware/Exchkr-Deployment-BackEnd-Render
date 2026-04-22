package com.exchkr.club.management.dao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exchkr.club.management.model.entity.User;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmail(@Param("email") String email);

    // This query connects the User to their many Clubs via the UserClub mapping
    @Query("""
            SELECT uc.clubId as clubId, c.clubName as clubName, r.roleId as roleId, r.roleName as roleName 
            FROM UserClub uc
            JOIN Club c ON uc.clubId = c.clubId
            JOIN Role r ON uc.roleId = r.roleId
            WHERE uc.userId = :userId AND uc.status = 'Active'
            """)
    List<UserClubMembershipProjection> findAllMembershipsByUserId(@Param("userId") Long userId);

    @Query("SELECT r.roleName FROM Role r WHERE r.roleId = :roleId")
    Optional<String> findRoleNameByRoleId(@Param("roleId") Long roleId);
    
    // Joined with UserClub and took the data
    @Query("""
            SELECT u FROM User u 
            JOIN UserClub uc ON u.userId = uc.userId 
            WHERE uc.clubId = :clubId AND uc.status <> :status
            """)
    List<User> findAllByClubIdAndStatusNot(@Param("clubId") Long clubId, @Param("status") String status);
    
    // Joined with UserClub to count members belonging to a specific club
    @Query("""
            SELECT COUNT(u) FROM User u 
            JOIN UserClub uc ON u.userId = uc.userId 
            WHERE uc.clubId = :clubId AND uc.status = 'Active' AND u.status <> 'InActive'
            """)
    long countActiveMembersByClubId(@Param("clubId") Long clubId);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :encodedPassword, u.lastUpdatedOn = :updatedOn WHERE u.email = :email")
    int updatePasswordByEmail(@Param("email") String email, @Param("encodedPassword") String encodedPassword, @Param("updatedOn") Instant updatedOn);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :encodedPassword, u.lastUpdatedOn = :updatedOn WHERE u.userId = :userId")
    int updatePasswordByUserId(@Param("userId") Long userId, @Param("encodedPassword") String encodedPassword, @Param("updatedOn") Instant updatedOn);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = 'InActive', u.lastUpdatedOn = :updatedOn WHERE u.userId = :userId")
    int deactivateUser(@Param("userId") Long userId, @Param("updatedOn") Instant updatedOn);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.plaidAccessToken = :token, u.lastUpdatedOn = :updatedOn WHERE u.userId = :userId")
    int updatePlaidAccessToken(@Param("userId") Long userId, @Param("token") String token, @Param("updatedOn") Instant updatedOn);
}