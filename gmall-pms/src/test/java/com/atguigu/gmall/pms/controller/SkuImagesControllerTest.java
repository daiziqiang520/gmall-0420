package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.pms.service.SkuImagesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SkuImagesControllerTest {

    @Autowired
    private SkuImagesService skuImagesService;
    @Test
    void querySkuImagesBySkuId() {
        System.out.println(this.skuImagesService.querySkuImagesBySkuId(41L));
    }
}