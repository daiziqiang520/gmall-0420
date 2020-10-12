package com.atguigu.gmall.index;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.gmallannotation.GmallCache;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Around("@annotation(com.atguigu.gmall.common.gmallannotation.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{

        //获得切点方法的签名
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //获得方法对象
        Method method = signature.getMethod();

        //获得方法上的注解
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        //获得注解的属性
        String prefix = annotation.prefix();
        //获取返回值类型
        Class<?> returnType = method.getReturnType();

        //获取注解上的属性lock
        String lock = annotation.lock();

        //从目标方法中取参数
        List<Object> args = Arrays.asList(joinPoint.getArgs());

        //判断缓存中是否存在数据,存在直接命中返回
        String json = this.redisTemplate.opsForValue().get(prefix + args);
        if (StringUtils.isNotBlank(json)){
            return JSON.parseObject(json, returnType);
        }
        //如果缓存中未命中。加分布式锁
        RLock rlock = this.redissonClient.getLock(lock+args);
        try {
            rlock.lock();

            //再次判断缓存中是否已存在数据，如果有，直接返回
            String json2 = this.redisTemplate.opsForValue().get(prefix + args);
            if (StringUtils.isNotBlank(json2)){
                return JSON.parseObject(json2, returnType);
            }

            //如果没有
            //执行目标方法
            Object proceed = joinPoint.proceed(joinPoint.getArgs());

            //放入缓存，释放分布式锁
            //获取注解中的超时时间
            int timeout = annotation.timeout();
            //获取注解中的随机时间
            int random = annotation.random();
            if (proceed ==null){
                this.redisTemplate.opsForValue().set(prefix + args, JSON.toJSONString(proceed),timeout,TimeUnit.MINUTES);
            }else {
                this.redisTemplate.opsForValue().set(prefix + args, JSON.toJSONString(proceed), timeout + new Random().nextInt(random), TimeUnit.MINUTES);
            }
            return proceed;
        } finally {
            rlock.unlock();
        }

    }
}
