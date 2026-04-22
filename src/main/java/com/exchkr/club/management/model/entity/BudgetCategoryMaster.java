package com.exchkr.club.management.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "ecm_budget_category_master")
@Comment("Source of truth for categories available to a specific club for budget planning")
public class BudgetCategoryMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Long categoryId;

	@Column(name = "club_id", nullable = false)
	@Comment("The ID of the club that owns this category definition")
	private Long clubId;

	@Column(name = "category_name", nullable = false, length = 255)
	@Comment("Display name of the category (e.g., Marketing Materials, Food & Supplies)")
	private String categoryName;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at")
	private Instant updatedAt = Instant.now();

	// Lifecycle hook to automatically update the timestamp on changes
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public BudgetCategoryMaster() {
	}

	public BudgetCategoryMaster(Long clubId, String categoryName) {
		this.clubId = clubId;
		this.categoryName = categoryName;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public Long getClubId() {
		return clubId;
	}

	public void setClubId(Long clubId) {
		this.clubId = clubId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}