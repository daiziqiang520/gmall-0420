package com.atguigu.gmall.gateway.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
/*如果服务器允许跨域，需要在返回的响应头中携带下面信息：

        - Access-Control-Allow-Origin：可接受的域，是一个具体域名或者*（代表任意域名）
        - Access-Control-Allow-Credentials：是否允许携带cookie，默认情况下，cors不会携带cookie，除非这个值是true
        - Access-Control-Allow-Methods：允许访问的方式
        - Access-Control-Allow-Headers：允许携带的头
        - Access-Control-Max-Age：本次许可的有效时长，单位是秒，**过期之前的ajax请求就无需再次进行预检了**



        > 有关cookie：

        要想操作cookie，需要满足3个条件：

        - 服务的响应头中需要携带Access-Control-Allow-Credentials并且为true。
        - 浏览器发起ajax需要指定withCredentials 为true
        - 响应头中的Access-Control-Allow-Origin一定不能为*，必须是指定的域名*/
@Configuration
public class CorsFilterConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        //允许携带cookie信息
        config.setAllowCredentials(true);
        //配置头信息
        config.addAllowedHeader("*");
        //可接受的域，为了将来能够携带cookie，不能配置为*
        config.addAllowedOrigin("http://manager.gmall.com");
        config.addAllowedOrigin("http://www.gmall.com");
        config.addAllowedOrigin("http://index.gmall.com");
        config.addAllowedOrigin("http://gmall.com");
        //允许访问的方式
        config.addAllowedMethod("*");
        corsConfigurationSource.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(corsConfigurationSource);
    }
}
