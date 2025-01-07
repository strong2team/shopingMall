package goorm.server.timedeal.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import goorm.server.timedeal.dto.ResPurchase;
import goorm.server.timedeal.service.TimeDealService;

@SpringBootTest
class TestPurchaseControllerTest {

	@Autowired
	private TimeDealService timeDealService; // TimeDealService 직접 주입

	@Test
	void testConcurrentPurchases() throws InterruptedException {
		int numberOfThreads = 100; // 동시에 요청을 보낼 쓰레드 수
		ExecutorService executorService = Executors.newFixedThreadPool(32); // 스레드 풀 크기 제한
		CountDownLatch latch = new CountDownLatch(numberOfThreads); // 스레드 완료 대기용
		CountDownLatch startLatch = new CountDownLatch(1); // 동시 시작용

		Long dealId = 28L;  // 테스트할 타임딜 ID
		Long userId = 2L;  // 테스트할 유저 ID
		int quantity = 1;  // 구매 수량

		for (int i = 0; i < numberOfThreads; i++) {
			executorService.submit(() -> {
				try {
					startLatch.await(); // 모든 스레드가 준비될 때까지 대기
					timeDealService.testPurchaseTimeDeal(dealId, userId, quantity);
				} catch (IllegalStateException e) {
					// 재고 부족 예외 처리
					System.err.println("Stock unavailable: " + e.getMessage());
				} catch (Exception e) {
					// 기타 예외 처리
					e.printStackTrace();
				} finally {
					latch.countDown(); // 스레드 완료 시 카운트 감소
				}
			});
		}

		// 모든 스레드 동시에 시작
		startLatch.countDown();

		// 모든 스레드가 완료될 때까지 대기
		latch.await();

		// 남은 재고 확인
		int remainingStock = timeDealService.getRemainingStock(dealId);
		System.out.println("Remaining stock: " + remainingStock);

		// 재고가 정확히 0인지 검증
		assertEquals(0, remainingStock, "Stock should be 0 after all purchases");

		executorService.shutdown();
	}
}