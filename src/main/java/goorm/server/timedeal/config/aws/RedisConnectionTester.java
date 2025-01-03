package goorm.server.timedeal.config.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class RedisConnectionTester implements CommandLineRunner {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public void run(String... args) throws Exception {
		try {
			// PING 명령어로 연결 상태 확인
			String response = redisTemplate.getConnectionFactory().getConnection().ping();
			//System.out.println("Redis PING response: " + response);


		} catch (Exception e) {
			System.err.println("Redis connection failed: " + e.getMessage());
		}
	}
}
