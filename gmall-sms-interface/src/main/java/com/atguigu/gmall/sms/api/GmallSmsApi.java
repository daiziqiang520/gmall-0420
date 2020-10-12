package com.atguigu.gmall.sms.api;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


public interface GmallSmsApi {

    @PostMapping("sms/skubounds/save")
    public ResponseVo saveSales(@RequestBody SkuSaleVo skuSaleVo);

    //根据skuId查询营销信息
    @GetMapping("sms/skubounds/querySkuSaleBySkuId/{skuId}")
    public ResponseVo<List<ItemSaleVo>> querySkuSaleBySkuId(@PathVariable("skuId")Long skuId);
}
