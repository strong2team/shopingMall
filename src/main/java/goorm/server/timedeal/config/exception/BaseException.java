package goorm.server.timedeal.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BaseException extends Exception {

	private BaseResponseStatus status;

	// 예외를 BaseResponse 로 변환하여 반환
	public BaseResponse<Object> toResponse() {
		return new BaseResponse<>(status);
	}
}
