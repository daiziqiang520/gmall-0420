package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.Vo.SkuVo;
import com.atguigu.gmall.pms.Vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.Vo.SpuVo;
import com.atguigu.gmall.pms.client.GmallSmsFeignClient;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescService spuDescService;
    @Autowired
    private SpuAttrValueService spuAttrValueService;
    @Autowired
    private SkuService skuService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Autowired
    private GmallSmsFeignClient gmallSmsFeignClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    // 分页查询spu列表
    @Override
    public PageResultVo queryspuByCidPage(Long categoryId, PageParamVo pageParamVo) {

        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<SpuEntity>();
        //判断categoryId是否为0，如果是0怎检索所有
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }

        //取出关键字
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
        }
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(), wrapper
        );
        return new PageResultVo(page);
    }

    //商品spu，sku，分组，属性大保存
    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        //1.保存spu
        //1.1保存pms_spu
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        this.save(spuVo);
        Long spuId = spuVo.getId();

        //1.2保存pms_spu_desc描述信息
        List<String> spuImages = spuVo.getSpuImages();
        if (!CollectionUtils.isNotEmpty(spuImages)) {
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(StringUtils.join(spuImages, ","));
            this.spuDescService.save(spuDescEntity);
        }

        //1.3保存pms_spu_attr_value信息
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<SpuAttrValueEntity> attrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {

                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();

                //spuAttrValueEntity.setAttrId(spuAttrValueVo.getAttrId());
                //spuAttrValueEntity.setAttrName(spuAttrValueVo.getAttrName());
                //spuAttrValueEntity.setAttrValue(spuAttrValueVo.getAttrValue());
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                spuAttrValueEntity.setSort(0);
                return spuAttrValueEntity;
            }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(attrValueEntities);
        }

        //2.保存sku
        //2.1保存psm_sku
        List<SkuVo> skus = spuVo.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(skuVo -> {
            SkuEntity skuEntity = new SkuEntity();
            BeanUtils.copyProperties(skuVo, skuEntity);
            skuEntity.setSpuId(spuId);
            skuEntity.setCatagoryId(spuVo.getCategoryId());
            skuEntity.setBrandId(spuVo.getBrandId());
            List<String> images = skuVo.getImages();
            if (CollectionUtils.isNotEmpty(skus)) {
                skuEntity.setDefaultImage(StringUtils.isBlank(skuEntity.getDefaultImage()) ? images.get(0) : skuEntity.getDefaultImage());
            }

            this.skuService.save(skuEntity);
            Long skuId = skuEntity.getId();

            //2.2保存pms_sku_images
            if (CollectionUtils.isNotEmpty(images)) {
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setSort(0);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(skuEntity.getDefaultImage(), image) ? 1 : 0);
                    skuImagesEntity.setUrl(image);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntities);
            }

            //2.3保存pms_sku_attr_values
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (CollectionUtils.isNotEmpty(saleAttrs)) {
                List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrs.stream().map(saleAttr -> {
                    SkuAttrValueEntity skuAttrValueEntity = new SkuAttrValueEntity();
                    BeanUtils.copyProperties(saleAttr, skuAttrValueEntity);
                    skuAttrValueEntity.setSkuId(skuId);
                    skuAttrValueEntity.setSort(0);
                    return skuAttrValueEntity;
                }).collect(Collectors.toList());
                this.skuAttrValueService.saveBatch(skuAttrValueEntities);
            }

            //3.保存营销信息(不分先后了)
            //3.1保存sms_sku_ladder
            //3.2保存sms_sku_bounds
            //3.3保存sms_sku_full_reduction
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.gmallSmsFeignClient.saveSales(skuSaleVo);
            this.rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE", "item.insert", spuId);
        });
    }

}