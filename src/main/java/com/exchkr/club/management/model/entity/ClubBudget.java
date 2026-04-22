package com.exchkr.club.management.model.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "ecm_club_budgets")
public class ClubBudget {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "budget_id")
	private Long budgetId;

	@Column(name = "club_id", nullable = false)
	private Long clubId;

	@Column(name = "total_budget", nullable = false)
	private BigDecimal totalBudget;

	@Column(name = "fiscal_year", nullable = false)
	private Integer fiscalYear;

	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	@Column(name = "created_at")
	private Instant createdAt = Instant.now();

	@Column(name = "active_ind")
	private Integer activeInd = 1;

	@OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<BudgetCategory> categories = new ArrayList<>();

	public void addCategory(BudgetCategory category) {
		categories.add(category);
		category.setBudget(this);
	}

	public Long getBudgetId() {
		return budgetId;
	}

	public void setBudgetId(Long budgetId) {
		this.budgetId = budgetId;
	}

	public Long getClubId() {
		return clubId;
	}

	public void setClubId(Long clubId) {
		this.clubId = clubId;
	}

	public BigDecimal getTotalBudget() {
		return totalBudget;
	}

	public void setTotalBudget(BigDecimal totalBudget) {
		this.totalBudget = totalBudget;
	}

	public Integer getFiscalYear() {
		return fiscalYear;
	}

	public void setFiscalYear(Integer fiscalYear) {
		this.fiscalYear = fiscalYear;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Integer getActiveInd() {
		return activeInd;
	}

	public void setActiveInd(Integer activeInd) {
		this.activeInd = activeInd;
	}

	public List<BudgetCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<BudgetCategory> categories) {
		this.categories = categories;
	}

}