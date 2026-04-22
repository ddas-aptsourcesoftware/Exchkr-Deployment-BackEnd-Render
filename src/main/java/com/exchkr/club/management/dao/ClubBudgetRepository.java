package com.exchkr.club.management.dao;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exchkr.club.management.model.entity.ClubBudget;

@Repository
public interface ClubBudgetRepository extends JpaRepository<ClubBudget, Long> {
	Optional<ClubBudget> findByClubIdAndFiscalYearAndActiveInd(Long clubId, Integer fiscalYear, Integer activeInd);
	
	@Query("SELECT COUNT(cb) > 0 FROM ClubBudget cb " +
	           "WHERE cb.clubId = :clubId " +
	           "AND cb.fiscalYear = :fiscalYear " +
	           "AND cb.activeInd = :activeInd")
	    boolean existsByClubIdAndFiscalYearAndActiveInd(
	        @Param("clubId") Long clubId, 
	        @Param("fiscalYear") Integer fiscalYear, 
	        @Param("activeInd") Integer activeInd
	    );
	
	/**
     * Updates the total_spent for a specific category within the active budget.
     * Uses a native query for atomic addition in the database.
     */
    @Modifying
    @Query(value = "UPDATE ecm_club_budget_categories bc " +
           "SET total_spent = bc.total_spent + :amount " +
           "FROM ecm_club_budgets b " +
           "WHERE bc.budget_id = b.budget_id " +
           "AND b.club_id = :clubId " +
           "AND b.fiscal_year = :year " +
           "AND b.active_ind = 1 " +
           "AND bc.category_name = :categoryName", nativeQuery = true)
    void updateSpentAmount(@Param("clubId") Long clubId, 
                          @Param("year") Integer year, 
                          @Param("categoryName") String categoryName, 
                          @Param("amount") BigDecimal amount);
}