package com.example.controller;

import com.example.manager.RedisLockImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import java.util.concurrent.locks.Lock;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    Lock lock = null;

    @PostConstruct
    public void init() {
        this.lock = new RedisLockImpl(stringRedisTemplate, "test", 3);
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    public void test() {
        lock.lock();
        try {
            System.out.println("get lock");
        } finally {
            lock.unlock();
        }
    }
}
