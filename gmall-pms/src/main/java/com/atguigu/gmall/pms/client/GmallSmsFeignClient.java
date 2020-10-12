package com.atguigu.gmall.pms.client;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient("sms-service")
public interface GmallSmsFeignClient extends GmallSmsApi {

}
