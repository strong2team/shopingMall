package goorm.server.timedeal.viewcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 타임딜 관리자 화면
 * */
@RequestMapping("/v1/admin/deals")
@Controller
public class AdminPageController {

	@GetMapping("")
	public String showTimeDealReservationPage(Model model) {
		return "deal_admin";  // 뷰를 반환
	}
}
