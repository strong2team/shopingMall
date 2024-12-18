package goorm.server.timedeal.model.enums;

public enum TimeDealStatus {
	ACTIVE("active"),   // 타임딜 활성 상태
	ENDED("ended"),    // 타임딜 종료 상태
	DELETED("deleted"); // 타임딜 삭제 상태

	private final String status;

	// Constructor
	TimeDealStatus(String status) {
		this.status = status;
	}

	// Getter
	public String getStatus() {
		return this.status;
	}

	// Optional: String 값으로 TimeDealStatus 찾기 (예: "active" -> ACTIVE)
	public static TimeDealStatus fromString(String status) {
		for (TimeDealStatus tds : TimeDealStatus.values()) {
			if (tds.status.equalsIgnoreCase(status)) {
				return tds;
			}
		}
		throw new IllegalArgumentException("No enum constant with status " + status);
	}
}
