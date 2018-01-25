package com.plivo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.plivo.config.PlivoSmsAppConfiguration;

import ratpack.rx.RxRatpack;
import ratpack.spring.config.EnableRatpack;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class,
		RedisAutoConfiguration.class })
@EnableRatpack
@Import(value={PlivoSmsAppConfiguration.class})
public class PlivoSMSApplication {

	public static void main(String[] args) {
		RxRatpack.initialize();
		SpringApplication.run(PlivoSMSApplication.class, args);
	}
}
