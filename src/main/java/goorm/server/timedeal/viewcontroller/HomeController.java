package goorm.server.timedeal.viewcontroller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import goorm.server.timedeal.dto.IndexPageTimeDealDto;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.enums.TimeDealStatus;
import goorm.server.timedeal.service.TimeDealService;
import lombok.extern.slf4j.Slf4j;

/**
 * 타임딜 메인 화면
 * */
import java.util.stream.Collectors;

@Slf4j
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

		// ACTIVE 상태만 필터링 및 DTO로 변환
		List<IndexPageTimeDealDto> activeDeals = timeDeals.stream()
			.filter(deal -> deal.getStatus() == TimeDealStatus.ACTIVE)
			.map(deal -> new IndexPageTimeDealDto(
				deal.getProduct().getProductId(),
				deal.getProduct().getProductImages().get(0).getImageUrl(),
				deal.getProduct().getTitle(),
				deal.getProduct().getPrice(),
				deal.getDiscountPrice(),
				deal.getDiscountPercentage() != null ? deal.getDiscountPercentage() + "%" : "",
				deal.getStartTime(),
				deal.getEndTime(),
				deal.getStatus().name(),
				deal.getStockQuantity()
			))
			.collect(Collectors.toList());

		// SCHEDULED 상태만 필터링 및 DTO 로 변환
		List<IndexPageTimeDealDto> scheduledDeals = timeDeals.stream()
			.filter(deal -> deal.getStatus() == TimeDealStatus.SCHEDULED)
			.map(deal -> new IndexPageTimeDealDto(
				deal.getProduct().getProductId(),
				deal.getProduct().getProductImages().get(0).getImageUrl(),
				deal.getProduct().getTitle(),
				deal.getProduct().getPrice(),
				deal.getDiscountPrice(),
				deal.getDiscountPercentage() != null ? String.valueOf(deal.getDiscountPercentage()) : "",
				deal.getStartTime(),
				deal.getEndTime(),
				deal.getStatus().name(),
				deal.getStockQuantity()
			))
			.collect(Collectors.toList());

		System.out.println("scheduledDeals.size() = " + scheduledDeals.size());
		System.out.println("scheduledDeals = " + scheduledDeals);

		System.out.println("activeDeals = " + activeDeals);

		model.addAttribute("activeDeals", activeDeals);
		model.addAttribute("scheduledDeals", scheduledDeals);

		return "index";
	}
}
