package com.exchkr.club.management.model.api.response;

import java.math.BigDecimal;
import java.util.List;

public class BudgetSummaryResponse {
	private Long budgetId;
	private Integer fiscalYear;
	private BigDecimal totalAnnualBudget;
	private BigDecimal totalAllocated; // Sum of all category budgets
	private BigDecimal totalSpent; // Sum of all actual spending
	private BigDecimal remainingAmount; // totalAnnualBudget - totalSpent
	private List<CategoryDetail> categories;

	public BudgetSummaryResponse() {
	}

	// Inner class for category breakdown
	public static class CategoryDetail {
		private String categoryName;
		private BigDecimal budgeted;
		private BigDecimal spent;
		private BigDecimal remaining;
		private Double percentageUsed;

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}

		public BigDecimal getBudgeted() {
			return budgeted;
		}

		public void setBudgeted(BigDecimal budgeted) {
			this.budgeted = budgeted;
		}

		public BigDecimal getSpent() {
			return spent;
		}

		public void setSpent(BigDecimal spent) {
			this.spent = spent;
		}

		public BigDecimal getRemaining() {
			return remaining;
		}

		public void setRemaining(BigDecimal remaining) {
			this.remaining = remaining;
		}

		public Double getPercentageUsed() {
			return percentageUsed;
		}

		public void setPercentageUsed(Double percentageUsed) {
			this.percentageUsed = percentageUsed;
		}

	}

	public Long getBudgetId() {
		return budgetId;
	}

	public void setBudgetId(Long budgetId) {
		this.budgetId = budgetId;
	}

	public Integer getFiscalYear() {
		return fiscalYear;
	}

	public void setFiscalYear(Integer fiscalYear) {
		this.fiscalYear = fiscalYear;
	}

	public BigDecimal getTotalAnnualBudget() {
		return totalAnnualBudget;
	}

	public void setTotalAnnualBudget(BigDecimal totalAnnualBudget) {
		this.totalAnnualBudget = totalAnnualBudget;
	}

	public BigDecimal getTotalAllocated() {
		return totalAllocated;
	}

	public void setTotalAllocated(BigDecimal totalAllocated) {
		this.totalAllocated = totalAllocated;
	}

	public BigDecimal getTotalSpent() {
		return totalSpent;
	}

	public void setTotalSpent(BigDecimal totalSpent) {
		this.totalSpent = totalSpent;
	}

	public BigDecimal getRemainingAmount() {
		return remainingAmount;
	}

	public void setRemainingAmount(BigDecimal remainingAmount) {
		this.remainingAmount = remainingAmount;
	}

	public List<CategoryDetail> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryDetail> categories) {
		this.categories = categories;
	}

}