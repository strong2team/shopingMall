package goorm.server.timedeal.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 공통 로깅 유틸리티 클래스.
 * HTTP 요청 및 에러 처리를 위한 로깅 메서드 포함.
 */
public class AppLogger {
    private static final Logger logger = LoggerFactory.getLogger("app-log");

    /**
     * 비즈니스 이벤트 로깅 메서드.
     *
     * @param event  이벤트 이름(예: "HTTP Request")
     * @param details  이벤트와 관련된 추가 정보(키-값 쌍으로 전달)
     */
    public static void logBusinessEvent(String event, Object... details) {
        logger.info("Business Event: {}, Details: {}", event, details);
    }

    /**
     * 에러 로깅 메서드.
     *
     * @param errorMessage  에러 메시지(예: "Request processing failed")
     * @param e  발생한 예외 객체
     * @param details  에러와 관련된 추가 정보(키-값 쌍으로 전달)
     */
    public static void logError(String errorMessage, Throwable e, Object... details) {
        logger.error("Error: {}, Details: {}, StackTrace: ", errorMessage, details, e);
    }

    /**
     * 성능 측정을 위한 로깅 메서드.
     *
     * @param step  성능 측정 단계(예: "Redis Query", "Lock Acquisition")
     * @param duration  소요 시간 (나노초 단위)
     * @param details  추가 정보(키-값 쌍으로 전달)
     */
    public static void logPerformance(String step, long duration, Object... details) {
        logger.info("Performance: {}, Duration: {} ns, Details: {}", step, duration, details);
    }
}