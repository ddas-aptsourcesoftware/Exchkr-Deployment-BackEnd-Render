package com.exchkr.club.management.model.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import jakarta.persistence.*;

@Entity
@Table(name = "ecm_club_budget_categories")
public class BudgetCategory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long allocationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "budget_id")
	private ClubBudget budget;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id", nullable = false)
	private BudgetCategoryMaster categoryMaster;

	private BigDecimal totalBudgeted;
	private BigDecimal totalSpent = BigDecimal.ZERO;

	public Long getAllocationId() {
		return allocationId;
	}

	public void setAllocationId(Long allocationId) {
		this.allocationId = allocationId;
	}

	public ClubBudget getBudget() {
		return budget;
	}

	public void setBudget(ClubBudget budget) {
		this.budget = budget;
	}

	public BudgetCategoryMaster getCategoryMaster() {
		return categoryMaster;
	}

	public void setCategoryMaster(BudgetCategoryMaster categoryMaster) {
		this.categoryMaster = categoryMaster;
	}

	public BigDecimal getTotalBudgeted() {
		return totalBudgeted;
	}

	public void setTotalBudgeted(BigDecimal totalBudgeted) {
		this.totalBudgeted = totalBudgeted;
	}

	public BigDecimal getTotalSpent() {
		return totalSpent;
	}

	public void setTotalSpent(BigDecimal totalSpent) {
		this.totalSpent = totalSpent;
	}

}