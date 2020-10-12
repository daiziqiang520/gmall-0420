package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 * 
 * @author qiangge
 * @email 23884280@qq.com
 * @date 2020-09-21 19:24:54
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {

    List<SkuAttrValueEntity> querySkuSaleValuesBySpuId(Long spuId);

    List<Map<String, Object>> queryskuAttrsByspuIdwithskuId(Long spuId);
}
