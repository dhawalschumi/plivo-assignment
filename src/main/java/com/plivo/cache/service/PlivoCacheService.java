/**
 * 
 */
package com.plivo.cache.service;

import com.plivo.cache.request.PlivoCacheRequest;

import rx.Observable;

/**
 * @author Dhawal
 *
 */
public interface PlivoCacheService {

	public Observable<String> get(PlivoCacheRequest cacheRequest);

	public Observable<Void> save(PlivoCacheRequest cacheRequest);

	public Observable<Void> delete(PlivoCacheRequest cacheRequest);

	public Observable<Long> incrementCounter(PlivoCacheRequest cacheRequest);

}
