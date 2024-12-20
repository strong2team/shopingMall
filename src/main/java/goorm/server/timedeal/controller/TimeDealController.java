package goorm.server.timedeal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goorm.server.timedeal.config.BaseResponse;
import goorm.server.timedeal.config.BaseResponseStatus;
import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.enums.UserRole;
import goorm.server.timedeal.service.TimeDealService;
import goorm.server.timedeal.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/time-deals")
public class TimeDealController {

	private final TimeDealService timeDealService;
	private final UserService userService;

	private boolean isAdminUser(Long userId){

		return userService.isUserRoleByUserId(userId, UserRole.TIME_DEAL_MANAGER);
	}

	@PostMapping
	public ResponseEntity<BaseResponse<TimeDeal>> createTimeDeal(@RequestBody ReqTimeDeal timeDealRequest) {

		BaseResponse<TimeDeal> response;

		try {
			// 관리자 여부 확인
			if (isAdminUser(timeDealRequest.userId())) {

				// 타임딜 생성
				TimeDeal timeDeal = timeDealService.createTimeDeal(timeDealRequest);

				// 성공 응답 반환
				response = new BaseResponse<>(BaseResponseStatus.SUCCESS, timeDeal);
				return new ResponseEntity<>(response, HttpStatus.CREATED);  // 성공적으로 생성
			} else {
				// 권한이 없는 경우
				response = new BaseResponse<>(BaseResponseStatus.FORBIDDEN);
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);  // 403 Forbidden
			}
		} catch (Exception e) { // 예외 발생 시 실패 응답
			response = new BaseResponse<>(BaseResponseStatus.ERROR);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}