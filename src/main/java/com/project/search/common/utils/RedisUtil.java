/**   
 * @Title: RedisUtil.java
 * @Package com.mindmedia.dealers.util
 * @Description: redis工具类
 * @author 董
 * @date 2017年2月14日 下午5:10:55
 * @version V1.0   
 */
package com.project.search.common.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: RedisUtil
 * @Description: redis工具类
 * @author 董
 * @date 2017年2月14日 下午5:10:55
 * 
 */
@Component
public class RedisUtil {

	@Resource
	private RedisTemplate redisTemplate;

	/**
	 * 判断缓存中是否有对应的value
	 * 
	 * @param key
	 * @return
	 */
	public boolean exists(final String key) {
		return redisTemplate.hasKey(key);
	}

	/**
	 * 将键值对设定一个指定的时间timeout.
	 * 
	 * @param key
	 * @param timeout
	 *            键值对缓存的时间，单位是毫秒
	 * @return 设置成功返回true，否则返回false
	 */
	public boolean tryLock(String key, long timeout) {
		boolean isSuccess = redisTemplate.opsForValue().setIfAbsent(key, "");
		if (isSuccess) {
			redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
		}
		return isSuccess;
	}

	/**
	 * 读取缓存
	 * 
	 * @param key
	 * @return
	 */
	public Object get(final String key) {
		Object result = null;
		ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
		result = operations.get(key);
		return result;
	}

	/**
	 * 删除对应的value
	 * 
	 * @param key
	 */
	public void remove(final String key) {
		if (exists(key)) {
			redisTemplate.delete(key);
		}
	}

	/**
	 * 写入缓存
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(final String key, Object value) {
		return set(key, value, null);
	}

	/**
	 * 
	 * @Title: set
	 * @Description: 写入缓存带有效期
	 * @param key
	 * @param value
	 * @param expireTime
	 * @return boolean    返回类型
	 * @throws
	 */
	public boolean set(final String key, Object value, Long expireTime) {
		boolean result = false;
		try {
			ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			if (expireTime != null) {
				redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
			}
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

    /**
     * 递增
     * @param key 键
    //     * @param by 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta){
        if(delta<0){
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     * @param key 键
    //     * @param by 要减少几(大于0)
     * @return
     */
    public long decr(String key, long delta){
        if(delta<0){
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().decrement(key, delta);
    }
}
