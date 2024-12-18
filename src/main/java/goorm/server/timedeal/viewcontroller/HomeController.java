package goorm.server.timedeal.viewcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 타임딜 메인 화면
 * */
@Controller
public class HomeController {

	@GetMapping("/")
	public String index() {
		return "index";
	}
}
