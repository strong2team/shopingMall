package goorm.server.timedeal.viewcontroller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import goorm.server.timedeal.dto.ResTimeDealListDto;
import goorm.server.timedeal.service.TimeDealService;

/**
 * 타임딜 관리자 화면
 * */
@RequestMapping("/v1/admin/deals")
@Controller
public class AdminPageController {

	private final TimeDealService timeDealService;

	// 생성자 주입
	public AdminPageController(TimeDealService timeDealService) {
		this.timeDealService = timeDealService;
	}


	@GetMapping("")
	public String showTimeDealReservationPage(Model model) {
		// 타임딜 리스트 가져오기
		List<ResTimeDealListDto> timeDeals = timeDealService.getTimeDealList();

		model.addAttribute("timeDeals", timeDeals);

		return "deal_admin";
	}
}
