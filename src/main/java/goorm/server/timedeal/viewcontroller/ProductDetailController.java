package goorm.server.timedeal.viewcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import goorm.server.timedeal.dto.ResDetailPageTimeDealDto;
import goorm.server.timedeal.service.TimeDealService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/v1")
public class ProductDetailController {

	private final TimeDealService timeDealService;

	public ProductDetailController(TimeDealService timeDealService) {
		this.timeDealService = timeDealService;
	}

	@GetMapping("/products/{productId}")
	public String getProductDetailPage(@PathVariable Long productId, Model model) {
		ResDetailPageTimeDealDto productDetails = timeDealService.getTimeDealDetails(productId);
		model.addAttribute("productDetails", productDetails);
		return "deal_detail";
	}
}
