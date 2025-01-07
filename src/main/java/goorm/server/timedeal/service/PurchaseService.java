package goorm.server.timedeal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import goorm.server.timedeal.dto.ResPurchase;
import goorm.server.timedeal.model.Purchase;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;
import goorm.server.timedeal.model.enums.PurchaseStatus;
import goorm.server.timedeal.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PurchaseService {

	private final PurchaseRepository purchaseRepository;

	/**
	 * 구매 기록을 생성하고 저장하는 메서드
	 *
	 * @param timeDeal 구매한 타임딜
	 * @param user     구매한 유저
	 * @param quantity 구매 수량
	 * @return 구매 완료 메시지
	 */
	@Transactional
	public ResPurchase createPurchaseRecord(TimeDeal timeDeal, User user, int quantity) {
		Purchase purchase = new Purchase();
		purchase.setTimeDeal(timeDeal);
		purchase.setUser(user);
		purchase.setQuantity(quantity);
		purchase.setPurchaseTime(LocalDateTime.now());
		purchase.setStatus(PurchaseStatus.PURCHASED);
		purchaseRepository.save(purchase);

		return new ResPurchase(
			user.getUserId(),
			quantity,
			purchase.getPurchaseTime()
		);
	}
}
