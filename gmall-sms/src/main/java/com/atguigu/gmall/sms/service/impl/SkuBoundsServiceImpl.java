package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.sms.service.SkuFullReductionService;
import com.atguigu.gmall.sms.service.SkuLadderService;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {
    @Autowired
    private SkuLadderService skuLadderService;
    @Autowired
    private SkuFullReductionService skuFullReductionService;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public void saveSales(SkuSaleVo skuSaleVo) {
        if (skuSaleVo==null){
            return;
        }
        //3.保存营销信息(不分先后了)
        //3.1保存sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo, skuLadderEntity);
        skuLadderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        this.skuLadderService.save(skuLadderEntity);
        //3.2保存sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo, skuBoundsEntity);
        List<Integer> work = skuSaleVo.getWork();
        if (CollectionUtils.isNotEmpty(work) || work.size() == 4){
            skuBoundsEntity.setWork(work.get(0)*8 + work.get(1) *4 + work.get(2) * 2 + work.get(3) );
        }
        this.save(skuBoundsEntity);
        //3.3保存sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleVo.getFullAddOther());
        this.skuFullReductionService.save(skuFullReductionEntity);
    }

    @Override
    public List<ItemSaleVo> querySkuSaleBySkuId(Long skuId) {

        List<ItemSaleVo> itemSaleVos = new ArrayList<>();

        //满减
        SkuFullReductionEntity skuFullReductionEntity = this.skuFullReductionService.getOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        ItemSaleVo itemFullReductionSaleVo = new ItemSaleVo();
        itemFullReductionSaleVo.setType("满减");
        itemFullReductionSaleVo.setDesc("此商品满" + skuFullReductionEntity.getFullPrice() + "减去" + skuFullReductionEntity.getFullPrice());
        itemSaleVos.add(itemFullReductionSaleVo);

        //打折
        SkuLadderEntity skuLadderEntity = this.skuLadderService.getOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        ItemSaleVo itemLadderSaleVo = new ItemSaleVo();
        itemLadderSaleVo.setType("打折");
        itemLadderSaleVo.setDesc("此商品满" + skuLadderEntity.getFullCount() + "打"+skuLadderEntity.getDiscount().divide(new BigDecimal(10)) +"折");
        itemSaleVos.add(itemLadderSaleVo);

        //积分
        SkuBoundsEntity skuBoundsEntity = this.baseMapper.selectOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        ItemSaleVo itemBoundsSaleVo = new ItemSaleVo();
        itemBoundsSaleVo.setType("积分");
        itemBoundsSaleVo.setDesc("此商品购买赠送" + skuBoundsEntity.getGrowBounds() + "的成长积分，以及" + skuBoundsEntity.getBuyBounds() + "的购物积分");
        itemSaleVos.add(itemBoundsSaleVo);
        return itemSaleVos;
    }

}

