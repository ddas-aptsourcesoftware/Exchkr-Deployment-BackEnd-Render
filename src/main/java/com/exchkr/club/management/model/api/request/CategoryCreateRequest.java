package com.exchkr.club.management.model.api.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CategoryCreateRequest(@NotEmpty(message = "Category list cannot be empty") List<String> categories) {
}