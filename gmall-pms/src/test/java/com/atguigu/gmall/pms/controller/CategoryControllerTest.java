package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CategoryControllerTest {
    @Autowired
    private CategoryService categoryService;

    @Test
    void queryLev3CategoryAndLev2CategoryAndLev1CategoryByLev3CategoryId() {
        List<CategoryEntity> categoryEntities = this.categoryService.queryLev3CategoryAndLev2CategoryAndLev1CategoryByLev3CategoryId(225L);
        System.out.println(categoryEntities);
    }

}