package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;
    @Autowired
    private SkuService skuService;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchSkuAttrValuesByCidAndSkuId(Long categoryId, Long skuId) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("category_id", categoryId).eq("search_type", 1);
        List<AttrEntity> attrEntities = this.attrMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(attrEntities)) {
            List<SkuAttrValueEntity> attrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>().
                    eq("sku_id", skuId).in("attr_id", attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList())));
            return attrValueEntities;
        }
        return null;
    }

    @Override
    public List<SaleAttrValueVo> querySkuSaleValuesBySpuId(Long spuId) {
        //根据spuId取出所有的skuId
        /*List<SkuEntity> skuEntities = this.skuService.list(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        //查询所有skuId下的属性
        List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().in("sku_id", skuIds));
*/        //将所有的销售属性类型转换
        // [{attrId: 3, attrName: '颜色', attrValues: '白色','黑色','粉色'},
        // {attrId: 8, attrName: '内存', attrValues: '6G','8G','12G'},
        // {attrId: 9, attrName: '存储', attrValues: '128G','256G','512G'}
      List<SkuAttrValueEntity> skuAttrValueEntities =  this.skuAttrValueMapper.querySkuSaleValuesBySpuId(spuId);
      if (CollectionUtils.isNotEmpty(skuAttrValueEntities)){

          //将集合进行分组以attId分组
          Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
          // 创建一个List<SaleAttrValueVo>
          List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
          map.forEach((attrId, attrs) ->{
              SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
              // attrId
              saleAttrValueVo.setAttrId(attrId);
              // attrName
              saleAttrValueVo.setAttrName(attrs.get(0).getAttrName());
              //attrValues
              saleAttrValueVo.setAttrValues(attrs.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));
              saleAttrValueVos.add(saleAttrValueVo);
          });
          return saleAttrValueVos;
      }
        return null;
    }

    @Override
    public List<SkuAttrValueEntity> querySkuSaleValueBySkuId(Long skuId) {

        List<SkuAttrValueEntity> skuAttrValueEntities = this.baseMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));

        return skuAttrValueEntities;
    }

    @Override
    public String queryskuAttrsByspuIdwithskuId(Long spuId) {
        List<Map<String, Object>> skus = this.skuAttrValueMapper.queryskuAttrsByspuIdwithskuId(spuId);
        Map<String, Long> jsonMap = skus.stream().collect(Collectors.toMap(sku -> sku.get("attr_values").toString(), sku -> (Long) sku.get("sku_id")));
        return JSON.toJSONString(jsonMap);
    }

}