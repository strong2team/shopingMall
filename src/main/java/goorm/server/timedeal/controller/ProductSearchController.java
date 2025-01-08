package goorm.server.timedeal.controller;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import goorm.server.timedeal.config.exception.BaseResponse;
import goorm.server.timedeal.config.exception.BaseResponseStatus;

/**
 * Product 조회 및 타임딜 등록
 * */
@RestController
@RequestMapping("/api/products")
public class ProductSearchController {

	@Value("${naver.client-id}")
	private String clientId;

	@Value("${naver.client-secret}")
	private String clientSecret;

	@Value("${naver.api-url}")
	private String NAVER_API_URL;

	/**
	 * 상품을 검색하는 함수. (Naver Shopping API 사용)
	 * 주어진 검색 조건에 맞는 상품들을 데이터베이스에서 조회하여 반환.
	 *
	 * @param searchQuery 검색할 상품의 키워드나 조건. 예를 들어, 상품명, 브랜드, 카테고리 등.
	 * @param category 상품 카테고리. 선택적으로 필터링 가.
	 * @param priceRange 가격 범위. 선택적으로 필터링할 가능.
	 * @param pageNum 검색 결과 페이지 번호. 기본값은 1로 설정.
	 * @param pageSize 한 페이지에 표시될 상품의 수. 기본값은 10으로 설정. (최대 100페이지)
	 *
	 * @return 검색 조건에 맞는 상품들의 리스트. 조건에 맞는 상품이 없으면 빈 리스트를 반환.
	 */
	@GetMapping("")
	public ResponseEntity<BaseResponse<Map<String, Object>>> searchProducts(
		@RequestParam String query,
		@RequestParam(defaultValue = "1") int page) {

		int display = 10; // 페이지당 항목 수

		// 총 페이지 수 계산
		String apiUrl = NAVER_API_URL + "?query=" + query + "&display=" + display + "&start=1"; // 임시로 시작 페이지는 1로 설정

		// HTTP 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Naver-Client-Id", clientId);
		headers.set("X-Naver-Client-Secret", clientSecret);
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// API 호출
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);

		// 응답 데이터 처리
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> body = response.getBody();
		assert body != null;

		// 전체 검색 결과 수 확인
		int totalResults = (int) body.getOrDefault("total", 0);

		// totalResults 10으로 나누어 총 페이지 수 계산
		int totalPages = (int) Math.ceil((double) totalResults / display);

		// 페이지 번호가 총 페이지 수보다 크면 마지막 페이지로 조정
		if (page > totalPages) {
			page = totalPages;
		}

		// 페이지 번호에 맞게 시작 인덱스를 계산
		int start = (page - 1) * display + 1;

		if (start > 1000) { // start 값이 1000을 초과하지 않도록 제한 (API 자체 제한)
			start = 1000;
		}

		// API URL (start 값 수정)
		apiUrl = NAVER_API_URL + "?query=" + query + "&display=" + display + "&start=" + start;

		// API 호출
		response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
		body = response.getBody();
		assert body != null;

		result.put("items", body.get("items"));
		result.put("currentPage", page);
		result.put("total", totalResults);
		result.put("display", display);
		result.put("totalPages", totalPages);

		return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS, result));
	}
}
