package goorm.server.timedeal.viewcontroller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.enums.TimeDealStatus;
import goorm.server.timedeal.service.TimeDealService;

/**
 * 타임딜 메인 화면
 * */
@Controller
public class HomeController {


	private final TimeDealService timeDealService;

	public HomeController(TimeDealService timeDealService) {
		this.timeDealService = timeDealService;
	}

	@GetMapping("/")
	public String index(Model model) {
		// ACTIVE 및 SCHEDULED 상태의 타임딜 가져오기
		List<TimeDeal> timeDeals = timeDealService.getActiveAndScheduledDeals();

		// ACTIVE 상태만 필터링
		List<TimeDeal> activeDeals = timeDeals.stream()
			.filter(deal -> deal.getStatus() == TimeDealStatus.ACTIVE)
			.toList();

		// SCHEDULED 상태만 필터링
		List<TimeDeal> scheduledDeals = timeDeals.stream()
			.filter(deal -> deal.getStatus() == TimeDealStatus.SCHEDULED)
			.toList();

		// 모델에 추가하여 View 로 전달
		model.addAttribute("activeDeals", activeDeals);
		model.addAttribute("scheduledDeals", scheduledDeals);

		System.out.println("Active Deals: " + activeDeals);
		System.out.println("Scheduled Deals: " + scheduledDeals);


		return "index";
	}
}
