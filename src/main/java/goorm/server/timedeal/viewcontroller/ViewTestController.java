package goorm.server.timedeal.viewcontroller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewTestController {

	@GetMapping("/test")
	public String testView() {
		// "testView"라는 이름의 뷰를 반환
		// src/main/resources/templates/testView.html (Thymeleaf 경우) 또는
		// WEB-INF/views/testView.jsp (JSP 경우)가 필요
		return "render_test";
	}
}
