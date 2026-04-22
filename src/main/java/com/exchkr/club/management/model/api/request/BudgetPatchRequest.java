package com.exchkr.club.management.model.api.request;

import java.math.BigDecimal;
import java.util.List;

public class BudgetPatchRequest {
	private BigDecimal totalBudget;
	private List<CategoryPatchDTO> categoryUpdates;

	public BigDecimal getTotalBudget() {
		return totalBudget;
	}

	public void setTotalBudget(BigDecimal totalBudget) {
		this.totalBudget = totalBudget;
	}

	public List<CategoryPatchDTO> getCategoryUpdates() {
		return categoryUpdates;
	}

	public void setCategoryUpdates(List<CategoryPatchDTO> categoryUpdates) {
		this.categoryUpdates = categoryUpdates;
	}

	public static class CategoryPatchDTO {
		private Long categoryId; // The ID from ecm_budget_category_master
		private String categoryName; // Present for brand new categories
		private BigDecimal totalBudgeted;

		public Long getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(Long categoryId) {
			this.categoryId = categoryId;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}

		public BigDecimal getTotalBudgeted() {
			return totalBudgeted;
		}

		public void setTotalBudgeted(BigDecimal totalBudgeted) {
			this.totalBudgeted = totalBudgeted;
		}

	}
}