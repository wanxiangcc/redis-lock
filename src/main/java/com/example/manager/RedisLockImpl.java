package com.example.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * redis lock 实现
 *
 * @author wanxiang
 */
public class RedisLockImpl implements Lock {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockImpl.class);
    private StringRedisTemplate redisTemplate;
    private String resourceName;
    private int timeout;

    public RedisLockImpl(StringRedisTemplate redisTemplate, String resourceName, int timeout) {
        this.redisTemplate = redisTemplate;
        this.resourceName = "lock_" + resourceName;
        this.timeout = timeout;
    }

    @Override
    public void lock() {
        while (!tryLock()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("get lock " + resourceName + " " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean tryLock() {
        boolean lockResult = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                // set key value ex 1 nx   , nx is not exits, setNx is set if not exits
                return redisConnection.set(resourceName.getBytes(), "".getBytes(),
                        Expiration.seconds(timeout), RedisStringCommands.SetOption.SET_IF_ABSENT);
            }
        });
        return lockResult;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {

        return false;
    }

    @Override
    public void unlock() {
        redisTemplate.delete(resourceName);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
