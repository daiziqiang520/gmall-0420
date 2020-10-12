package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();
        //1.根据skuId查询sku相关信息
        ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
        SkuEntity skuEntity = skuEntityResponseVo.getData();
        if (skuEntity == null){
            return null;
        }
        itemVo.setSkuId(skuId);
        itemVo.setTitle(skuEntity.getTitle());
        itemVo.setSubTitle(skuEntity.getSubtitle());
        itemVo.setDefaultImage(skuEntity.getDefaultImage());
        itemVo.setWeight(skuEntity.getWeight());
        itemVo.setPrice(skuEntity.getPrice());

        //2.根据categoryId查询三级分类，在查询所有分类相关信息
        ResponseVo<List<CategoryEntity>> categoryListResponseVo = this.pmsClient.queryLev3CategoryAndLev2CategoryAndLev1CategoryByLev3CategoryId(skuEntity.getCatagoryId());
        List<CategoryEntity> categoryEntities = categoryListResponseVo.getData();
        itemVo.setCategoryEntities(categoryEntities);

        //3.根据brandId查询品品牌相关信息
        ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
        BrandEntity brandEntity = brandEntityResponseVo.getData();
        if (brandEntity != null){
            itemVo.setBrandId(skuEntity.getBrandId());
            itemVo.setBrandName(brandEntity.getName());
        }
        //4.根据spuId查新spu相关信息
        ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(skuEntity.getSpuId());
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if (spuEntity !=null){
            itemVo.setSpuId(spuEntity.getId());
            itemVo.setSpuName(spuEntity.getName());
        }

        //5.根据skuId查询sku图片信息
        ResponseVo<List<SkuImagesEntity>> skuImagesResponseVo = this.pmsClient.querySkuImagesBySkuId(skuId);
        List<SkuImagesEntity> skuImagesEntities = skuImagesResponseVo.getData();
        itemVo.setImages(skuImagesEntities);

        //6.根据skuId查询营销信息
        ResponseVo<List<ItemSaleVo>> skuSalesResponseVo = this.smsClient.querySkuSaleBySkuId(skuId);
        List<ItemSaleVo> skuSales = skuSalesResponseVo.getData();
        itemVo.setSales(skuSales);

        //7.根据skuId查询库存信息
        ResponseVo<List<WareSkuEntity>> wareSkuEntityResponseVo = this.wmsClient.queryWareSkuBykuId(skuId);
        List<WareSkuEntity> wareSkuEntities = wareSkuEntityResponseVo.getData();
        boolean store = wareSkuEntities.stream().anyMatch(wareSkuEntity -> {
            return wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0;
        });
        itemVo.setStore(store);

        //8.根据spuId查询spu下所有sku所有的销售属性
        ResponseVo<List<SaleAttrValueVo>> saleAttrValueVoResponseVo = this.pmsClient.querySkuSaleValuesBySpuId(skuEntity.getSpuId());
        List<SaleAttrValueVo> saleAttrValueVos = saleAttrValueVoResponseVo.getData();
        itemVo.setSaleAttrs(saleAttrValueVos);

        //9.根据skuId查询当前sku的销售属性
        ResponseVo<List<SkuAttrValueEntity>> SkuAttrValueEntityResponseVo = this.pmsClient.querySkuSaleValueBySkuId(skuId);
        List<SkuAttrValueEntity> skuAttrValueEntities = SkuAttrValueEntityResponseVo.getData();
        Map<Long, String> saleAttr = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
        itemVo.setSaleAttr(saleAttr);

        //10.根据spuId查询spu下所有的sku下的销售属性与skuId的映射关系
        ResponseVo<String> stringResponseVo = this.pmsClient.queryskuAttrsByspuIdwithskuId(skuEntity.getSpuId());
        String skusJson = stringResponseVo.getData();
        itemVo.setSkusJson(skusJson);

        //11.根据spuId查询spu描述信息
        ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.pmsClient.querySpuDescById(skuEntity.getSpuId());
        SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
        if (spuDescEntity != null){
            String decript = spuDescEntity.getDecript();
            String[] desc = StringUtils.split(decript, ",");
            itemVo.setSpuImages(Arrays.asList(desc));
        }

        //12.根据skuId，spuId,categoryId查询所有规格参数组以及规格参数组下的所有属性值
        ResponseVo<List<ItemGroupVo>> itemGroupResponseVo = this.pmsClient.queryGroupsBySpuIdAndCid(skuEntity.getCatagoryId(), skuId, skuEntity.getSpuId());
        List<ItemGroupVo> itemGroupVos = itemGroupResponseVo.getData();
        itemVo.setGroups(itemGroupVos);

        return itemVo;
    }
}
