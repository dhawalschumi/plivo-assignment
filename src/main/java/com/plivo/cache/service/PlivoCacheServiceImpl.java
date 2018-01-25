/**
 * 
 */
package com.plivo.cache.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.plivo.cache.request.PlivoCacheRequest;

import rx.Observable;

/**
 * @author Dhawal Patel
 *
 */
@Service
public class PlivoCacheServiceImpl implements PlivoCacheService {

	private static final Logger logger = LoggerFactory.getLogger(PlivoCacheServiceImpl.class);

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public Observable<String> get(PlivoCacheRequest cacheRequest) {
		return Observable.just(cacheRequest).flatMap(cacheRequestForGet -> {
			return Observable.just(redisTemplate.opsForValue().get(cacheRequestForGet.getKey()));
		}).onErrorReturn((Throwable e) -> "").doOnError((Throwable e) -> {
			logger.error("Exception while getting from cache for key = " + cacheRequest.getKey(), e);
		});
	}

	@Override
	public Observable<Void> save(PlivoCacheRequest cacheRequest) {
		return Observable.just(cacheRequest).doOnError((Throwable e) -> {
			logger.error("Exception while saving to cache for key = " + cacheRequest.getKey(), e);
		}).flatMap(cacheRequestToSave -> {
			redisTemplate.opsForValue().set(cacheRequest.getKey(), cacheRequest.getValue(), cacheRequest.getTtl(),
					TimeUnit.SECONDS);
			return Observable.empty();
		});
	}

	@Override
	public Observable<Void> delete(PlivoCacheRequest cacheRequest) {
		return Observable.just(cacheRequest).doOnError((Throwable e) -> {
			logger.error("Exception while deleting from cache for key = " + cacheRequest.getKey(), e);
		}).flatMap(cacheRequestTodelete -> {
			redisTemplate.delete(cacheRequestTodelete.getKey());
			return Observable.empty();
		});
	}

	@Override
	public Observable<Long> incrementCounter(PlivoCacheRequest cacheRequest) {
		return Observable.just(cacheRequest).flatMap(cacheRequestForGet -> {
			boolean result = redisTemplate.opsForValue().setIfAbsent(cacheRequestForGet.getKey(), "1");
			if (!result) {
				return Observable.just(redisTemplate.opsForValue().increment(cacheRequestForGet.getKey(), 1));
			} else {
				redisTemplate.expire(cacheRequest.getKey(), cacheRequest.getTtl(), TimeUnit.SECONDS);
				return Observable.just(1L);
			}
		}).onErrorReturn((Throwable e) -> -1L).doOnError((Throwable e) -> {
			logger.error("Exception while getting from cache for key = " + cacheRequest.getKey(), e);
		});
	}

}
