package goorm.server.timedeal.model.enums;

public enum UserRole {
	TIME_DEAL_MANAGER("TimeDealManager"), // 타임딜 관리자
	REGULAR_USER("RegularUser");           // 일반 사용자

	private final String role;

	// Constructor
	UserRole(String role) {
		this.role = role;
	}

	// Getter
	public String getRole() {
		return this.role;
	}

	// Optional: String 값으로 UserRole 찾기 (예: "TimeDealManager" -> TIME_DEAL_MANAGER)
	public static UserRole fromString(String role) {
		for (UserRole ur : UserRole.values()) {
			if (ur.role.equalsIgnoreCase(role)) {
				return ur;
			}
		}
		throw new IllegalArgumentException("No enum constant with role " + role);
	}
}
