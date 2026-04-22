package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.UserClub;
import com.exchkr.club.management.model.entity.UserClubId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserClubRepository extends JpaRepository<UserClub, UserClubId> {

    // 1. Basic Membership Checks 
    @Query("SELECT COUNT(uc) > 0 FROM UserClub uc WHERE uc.userId = :userId AND uc.clubId = :clubId")
    boolean existsByUserIdAndClubId(@Param("userId") Long userId, @Param("clubId") Long clubId);
    
    @Query("SELECT COUNT(uc) > 0 FROM UserClub uc WHERE uc.userId = :userId AND uc.clubId = :clubId AND uc.status = :status")
    boolean existsByUserIdAndClubIdAndStatus(@Param("userId") Long userId, @Param("clubId") Long clubId, @Param("status") String status);

    // 2. Fetch All Members for a Club
    @Query("""
    	       SELECT u as user, 
    	              r.roleName as roleName, 
    	              uc.roleId as roleId, 
    	              uc.clubId as clubId,
    	              uc.joinedAt as joinedAt 
    	       FROM UserClub uc 
    	       JOIN User u ON uc.userId = u.userId 
    	       JOIN Role r ON uc.roleId = r.roleId 
    	       WHERE uc.clubId = :clubId AND uc.status = 'Active'
    	       """)
    	List<UserClubMembershipProjection> findAllMembersByClubId(@Param("clubId") Long clubId);

    // 3. Find specific membership details
    @Query("""
           SELECT u as user, 
                  r.roleName as roleName, 
                  uc.roleId as roleId, 
                  c.clubName as clubName, 
                  uc.clubId as clubId 
           FROM UserClub uc 
           JOIN User u ON uc.userId = u.userId 
           JOIN Role r ON uc.roleId = r.roleId 
           JOIN Club c ON uc.clubId = c.clubId 
           WHERE uc.userId = :userId AND uc.clubId = :clubId AND uc.status = 'Active'
           """)
    Optional<UserClubMembershipProjection> findMembershipDetail(@Param("userId") Long userId, @Param("clubId") Long clubId);

    // 4. Count Active Members
    long countByClubIdAndStatus(Long clubId, String status);
    
    @Query("SELECT COUNT(uc) FROM UserClub uc WHERE uc.clubId = :clubId AND uc.status = :status AND uc.joinedAt >= :since")
    long countByClubIdAndStatusAndJoinedAtAfter(
        @Param("clubId") Long clubId, 
        @Param("status") String status, 
        @Param("since") java.time.Instant since
    );

    // 5. Soft Delete (Deactivate) membership
    @Modifying
    @Query("UPDATE UserClub uc SET uc.status = 'InActive' WHERE uc.userId = :userId AND uc.clubId = :clubId")
    void deactivateMembership(@Param("userId") Long userId, @Param("clubId") Long clubId);
}