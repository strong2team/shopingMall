package goorm.server.timedeal.model.enums;

public enum MessageType {
	USER_TIME_DEAL_CHANGE("UserTimeDealChange"),
	AUTO_TIME_DEAL_CHANGE("AutoTimeDealChange");

	private final String type;

	MessageType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	// Optional: 문자열로 Enum을 찾는 메서드
	public static MessageType fromString(String type) {
		for (MessageType messageType : MessageType.values()) {
			if (messageType.type.equalsIgnoreCase(type)) {
				return messageType;
			}
		}
		throw new IllegalArgumentException("Unknown message type: " + type);
	}
}
