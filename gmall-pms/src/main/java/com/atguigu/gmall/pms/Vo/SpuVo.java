package com.atguigu.gmall.pms.Vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

@Data
public class SpuVo extends SpuEntity {
    private List<?> spuImages;

    private List<?> baseAttrs;

    private List<?> skus;
}
