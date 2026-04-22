package com.exchkr.club.management.model.api.request;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class BudgetSetupRequest {
	@NotNull(message = "Total budget is required")
	private BigDecimal totalBudget;

	private List<CategoryAllocationDTO> categories;

	public BigDecimal getTotalBudget() {
		return totalBudget;
	}

	public void setTotalBudget(BigDecimal totalBudget) {
		this.totalBudget = totalBudget;
	}

	public List<CategoryAllocationDTO> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryAllocationDTO> categories) {
		this.categories = categories;
	}

	public static class CategoryAllocationDTO {
		@NotNull(message = "Category ID is required")
		private Long categoryId;
		private String categoryName;

		@NotNull(message = "Budgeted amount is required")
		@PositiveOrZero(message = "Budgeted amount cannot be negative")
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