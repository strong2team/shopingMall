package goorm.server.timedeal.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import goorm.server.timedeal.config.BaseResponse;
import goorm.server.timedeal.config.BaseResponseStatus;
import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.dto.ResDetailPageTimeDealDto;
import goorm.server.timedeal.dto.ResIndexPageTimeDealDto;
import goorm.server.timedeal.dto.UpdateReqTimeDeal;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.enums.TimeDealStatus;
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
	/**
	 * 새로운 타임딜을 생성하는 API.
	 *
	 * @param timeDealRequest 생성할 타임딜의 정보를 담고 있는 `ReqTimeDeal`.
	 * @return 생성된 타임딜을 포함한 응답을 반환.
	 */

	@PostMapping
	public ResponseEntity<BaseResponse<TimeDeal>> createTimeDeal(@RequestBody ReqTimeDeal timeDealRequest) {

		BaseResponse<TimeDeal> response;

		System.out.println("timeDealRequest = " + timeDealRequest.startTime());

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

	/**
	 * 타임딜의 정보를 수정하는 API.
	 *
	 * @param dealId 수정할 타임딜의 고유 ID.
	 * @param timeDealUpdateRequest 수정할 타임딜의 정보를 담고 있는 `UpdateReqTimeDeal` 객체.
	 * @return 수정된 타임딜을 포함한 응답을 반환.
	 */
	@PatchMapping("/{dealId}")
	public ResponseEntity<BaseResponse<TimeDeal>> updateTimeDeal(
		@PathVariable Long dealId,
		@RequestBody UpdateReqTimeDeal timeDealUpdateRequest) {

		BaseResponse<TimeDeal> response;

		try {
			// 관리자 여부 확인
			if (isAdminUser(timeDealUpdateRequest.userId())) {

				// 타임딜 상태 수정
				TimeDeal updatedTimeDeal = timeDealService.updateTimeDeal(dealId, timeDealUpdateRequest);

				// 성공 응답 반환
				response = new BaseResponse<>(BaseResponseStatus.SUCCESS, updatedTimeDeal);
				return new ResponseEntity<>(response, HttpStatus.OK);  // 수정 성공

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

	/**
	 * 상품 상세 정보를 조회하는 Json API.
	 *
	 * @param productId 조회할 상품의 ID.
	 * @return 상품 상세 정보를 포함한 응답.
	 */
	@GetMapping("/{productId}/details")
	public ResponseEntity<BaseResponse<ResDetailPageTimeDealDto>> getTimeDealDetails(@PathVariable Long productId) {
		BaseResponse<ResDetailPageTimeDealDto> response;

		try {
			// 상품 상세 정보 조회
			ResDetailPageTimeDealDto timeDealDetails = timeDealService.getTimeDealDetails(productId);

			// 성공 응답
			response = new BaseResponse<>(BaseResponseStatus.SUCCESS, timeDealDetails);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) { // 예외 발생 시 실패 응답
			response = new BaseResponse<>(BaseResponseStatus.ERROR);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 타임딜 구매 API
	 * @param dealId 구매할 타임딜 ID
	 * @param quantity 구매 수량
	 * @return 구매 성공 여부
	 */
	@PostMapping("/{dealId}/purchases")
	public ResponseEntity<BaseResponse<String>> purchaseTimeDeal(
		@PathVariable Long dealId,
		@RequestParam int quantity) {

		BaseResponse<String> response;

		try {
			String resultMessage = timeDealService.purchaseTimeDeal(dealId, quantity);
			response = new BaseResponse<>(BaseResponseStatus.SUCCESS, resultMessage);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IllegalStateException e) {
			// 재고 부족 등의 예외 처리
			response = new BaseResponse<>(BaseResponseStatus.STOCK_UNAVAILABLE, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			// 기타 예외 처리
			response = new BaseResponse<>(BaseResponseStatus.ERROR, "구매 실패: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/deals/{timeDealId}")
	public ResponseEntity<ResIndexPageTimeDealDto> getDealById(@PathVariable Long timeDealId) {
		Optional<TimeDeal> timeDealOpt = timeDealService.findById(timeDealId);

		if (timeDealOpt.isPresent()) {
			TimeDeal deal = timeDealOpt.get();
			ResIndexPageTimeDealDto dto = new ResIndexPageTimeDealDto(
				deal.getProduct().getProductId(),
				deal.getProduct().getProductImages().get(0).getImageUrl(),
				deal.getProduct().getTitle(),
				deal.getProduct().getPrice(),
				deal.getDiscountPrice(),
				deal.getDiscountPercentage() != null ? String.valueOf(Math.round(deal.getDiscountPercentage())) : "",
				deal.getStartTime(),
				deal.getEndTime(),
				deal.getStatus().name(),
				deal.getStockQuantity()
			);

			return ResponseEntity.ok(dto);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

}
