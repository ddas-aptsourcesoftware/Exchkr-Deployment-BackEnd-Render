package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.BudgetCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetCategoryMasterRepository extends JpaRepository<BudgetCategoryMaster, Long> {

	List<BudgetCategoryMaster> findByClubIdOrderByCategoryNameAsc(Long clubId);

	@Query("SELECT COUNT(b) > 0 FROM BudgetCategoryMaster b " +
	           "WHERE b.clubId = :clubId AND UPPER(b.categoryName) = UPPER(:categoryName)")
	    boolean existsByClubIdAndCategoryNameIgnoreCase(
	        @Param("clubId") Long clubId, 
	        @Param("categoryName") String categoryName
	    );
	
	Optional<BudgetCategoryMaster> findByClubIdAndCategoryNameIgnoreCase(Long clubId, String categoryName);
}