package com.exchkr.club.management.model.api.response;

public class MemberCountResponse {
	private long totalCount;
	private long joinedThisMonthCount;

	public MemberCountResponse(long totalCount, long joinedThisMonthCount) {
		this.totalCount = totalCount;
		this.joinedThisMonthCount = joinedThisMonthCount;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public long getJoinedThisMonthCount() {
		return joinedThisMonthCount;
	}

	public void setJoinedThisMonthCount(long joinedThisMonthCount) {
		this.joinedThisMonthCount = joinedThisMonthCount;
	}
}