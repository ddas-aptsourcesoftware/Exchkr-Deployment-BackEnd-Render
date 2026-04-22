package com.exchkr.club.management.model.api.request;

import com.google.firebase.database.annotations.NotNull;

public record DueReminderRequest(
	@NotNull Long dueId,
	@NotNull Long memberId
) {}