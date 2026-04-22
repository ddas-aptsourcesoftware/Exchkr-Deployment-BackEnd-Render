package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.Club;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    @Query("SELECT c.clubName FROM Club c WHERE c.clubId = :clubId")
    Optional<String> findClubNameById(@Param("clubId") Long clubId);
    
    @Query("SELECT COUNT(c) > 0 FROM Club c WHERE c.clubName = :clubName AND c.schoolName = :schoolName")
    boolean existsByClubNameAndSchoolName(@Param("clubName") String clubName, @Param("schoolName") String schoolName);
}