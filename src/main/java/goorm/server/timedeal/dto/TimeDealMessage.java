package goorm.server.timedeal.dto;

public class TimeDealMessage {
	private Long time_deal_id;
	private String new_status;

	@Override
	public String toString() {
		return "TimeDealMessage{" +
			"time_deal_id=" + time_deal_id +
			", new_status='" + new_status + '\'' +
			'}';
	}

	public Long getTime_deal_id() {
		return time_deal_id;
	}

	public void setTime_deal_id(Long time_deal_id) {
		this.time_deal_id = time_deal_id;
	}

	public String getNew_status() {
		return new_status;
	}

	public void setNew_status(String new_status) {
		this.new_status = new_status;
	}
}

