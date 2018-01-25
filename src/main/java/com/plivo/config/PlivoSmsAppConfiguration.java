/**
 * 
 */
package com.plivo.config;

import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.plivo.authentication.service.PlivoAuthenticationService;
import com.plivo.logger.PlivoNcsaRequestLogger;
import com.plivo.request.handlers.PlivoInboundSmsHandler;
import com.plivo.request.handlers.PlivoOutboundSmsRequestHandler;

import ratpack.func.Action;
import ratpack.handling.Chain;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Dhawal Patel
 *
 */
@Configuration
@ComponentScan(basePackages = { "com.plivo" })
public class PlivoSmsAppConfiguration {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public ObjectWriter objectWriter(@Autowired ObjectMapper objectMapper) {
		return objectMapper.writer();
	}

	@Bean
	public Action<Chain> handlerConfiguration() {
		return chain -> {
			PlivoNcsaRequestLogger logger = chain.getRegistry().get(PlivoNcsaRequestLogger.class);
			PlivoAuthenticationService plivoAuthenticationService = chain.getRegistry()
					.get(PlivoAuthenticationService.class);
			PlivoInboundSmsHandler plivoInboundSmsHandler = chain.getRegistry().get(PlivoInboundSmsHandler.class);
			PlivoOutboundSmsRequestHandler plivoOutboundSmsHandler = chain.getRegistry()
					.get(PlivoOutboundSmsRequestHandler.class);
			chain.all(logger);
			chain.all(plivoAuthenticationService);
			chain.post("inbound/sms", plivoInboundSmsHandler);
			chain.post("outbound/sms", plivoOutboundSmsHandler);
		};
	}

	@Bean
	public JedisConnectionFactory getRedisTemplate(@Value("${plivo.redis.cluster.enabled}") boolean redisClusterEnabled,
			@Value("${plivo.redis.server.hosts}") String redisServerHosts,
			@Value("${plivo.redis.server.password}") String redisServerPassword,
			@Value("${plivo.redis.max.connections}") int redisMaxConnections,
			@Value("${plivo.redis.max.idle.connections}") int redisMaxIdleConnections) {

		JedisConnectionFactory connectionFactory = null;
		if (Boolean.valueOf(redisClusterEnabled)) {
			Set<String> redisServer = new HashSet<>(StringUtils.commaDelimitedListToSet(redisServerHosts));
			RedisClusterConfiguration redisClusterConfig = new RedisClusterConfiguration(redisServer);
			connectionFactory = new JedisConnectionFactory(redisClusterConfig);
		} else {
			connectionFactory = new JedisConnectionFactory();
			String[] redisServerHostPort = redisServerHosts.split(":");
			connectionFactory.setHostName(redisServerHostPort[0].trim());
			connectionFactory.setPort(Integer.valueOf(redisServerHostPort[1].trim()));
		}
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(redisMaxIdleConnections);
		poolConfig.setMaxTotal(redisMaxConnections);
		connectionFactory.setPassword(redisServerPassword);
		connectionFactory.setPoolConfig(poolConfig);
		return connectionFactory;
	}

	@Bean
	public DataSource dataSource(@Value("${plivo.database.user.name}") String dbUserName,
			@Value("${plivo.database.user.password}") String dbPassword, @Value("${plivo.database.url}") String dbUrl,
			@Value("${plivo.database.min.idle.connection}") int minIdle,
			@Value("${plivo.database.max.idle.connection}") int maxIdle,
			@Value("${plivo.database.max.total.connection}") int maxTotal,
			@Value("${plivo.database.validation.query}") String validationQuery) {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUsername(dbUserName);
		dataSource.setPassword(dbPassword);
		dataSource.setUrl(dbUrl);
		dataSource.setMaxIdle(maxIdle);
		dataSource.setValidationQuery(validationQuery);
		dataSource.setMaxTotal(maxTotal);
		dataSource.setMinIdle(minIdle);
		return dataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		return template;
	}

	@Bean
	public StringRedisTemplate redisTemplate(@Autowired JedisConnectionFactory jedisFactory) {
		return new StringRedisTemplate(jedisFactory);
	}
}
