package goorm.server.timedeal.viewcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TimeDealPageController {

	@GetMapping("/timedeal/reservation")
	public String showTimeDealReservationPage(Model model) {
		return "deal_admin";  // timedealReservation.html 뷰를 반환
	}
}
