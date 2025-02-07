package goorm.server.timedeal.config.aws.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RedisConfig {

	@Bean
	public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
		MessageListenerAdapter listenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new org.springframework.data.redis.listener.ChannelTopic("time-deal-channel"));

		log.info("Subscribed to channel: time-deal-channel");
		return container;
	}

	@Bean
	public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
		return new MessageListenerAdapter(subscriber);
	}
}
