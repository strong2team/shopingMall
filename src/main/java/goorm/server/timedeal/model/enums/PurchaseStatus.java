package goorm.server.timedeal.model.enums;

public enum PurchaseStatus {
	PURCHASED("purchased"),    // 구매 완료
	CANCELED("canceled"),      // 구매 취소
	PENDING("pending");        // 구매 대기 중

	private final String status;

	// Constructor
	PurchaseStatus(String status) {
		this.status = status;
	}

	// Getter
	public String getStatus() {
		return this.status;
	}

	// Optional: String 값으로 PurchaseStatus 찾기 (예: "purchased" -> PURCHASED)
	public static PurchaseStatus fromString(String status) {
		for (PurchaseStatus ps : PurchaseStatus.values()) {
			if (ps.status.equalsIgnoreCase(status)) {
				return ps;
			}
		}
		throw new IllegalArgumentException("No enum constant with status " + status);
	}
}
