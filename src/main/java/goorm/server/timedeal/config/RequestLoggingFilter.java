package goorm.server.timedeal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, jakarta.servlet.ServletException {

        long startTime = System.currentTimeMillis(); // 요청 시작 시간 기록

        try {
            filterChain.doFilter(request, response); // 요청 처리
        } finally {
            long duration = System.currentTimeMillis() - startTime; // 처리 시간 계산
            int status = response.getStatus(); // 응답 상태 코드 가져오기
            String method = request.getMethod(); // HTTP 메서드
            String uri = request.getRequestURI(); // 요청 URI

            // 로그 출력
            logger.info("HTTP {} {} - Status: {}, Response time: {}ms", method, uri, status, duration);
        }
    }
}