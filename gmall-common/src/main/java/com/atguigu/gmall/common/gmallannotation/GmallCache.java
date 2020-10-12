package com.atguigu.gmall.common.gmallannotation;

import java.lang.annotation.*;

@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /*
    缓存的前缀
    */
    String prefix() default "";

    //设置缓存的有限时间。单位：分钟
    int timeout() default 5;

    //分布式锁key
    String lock() default "lock";

    //防止雪崩的随机时间

    int random() default 5;

}
