package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author qiangge
 * @email 23884280@qq.com
 * @date 2020-09-21 19:24:54
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySearchSkuAttrValuesByCidAndSkuId(Long categoryId, Long skuId);

    List<SaleAttrValueVo> querySkuSaleValuesBySpuId(Long spuId);

    List<SkuAttrValueEntity> querySkuSaleValueBySkuId(Long skuId);

    String queryskuAttrsByspuIdwithskuId(Long skuId);
}

