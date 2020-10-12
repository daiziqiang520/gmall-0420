package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.client.GmallPmsClient;
import com.atguigu.gmall.search.client.GmallWmsClient;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private GoodsRepository repository;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;

    //将mysql中的数据导入es中
    @Test
    void contextLoads() {
        //创建索引库和类型
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
        //设置分页参数
        Integer pageNum = 1;
        Integer pageSize = 100;
        //循环遍历保存数据
        do {
            //1.分批查询spu数据
            PageParamVo pageParamVo = new PageParamVo();
            pageParamVo.setPageNum(pageNum);
            pageParamVo.setPageSize(pageSize);
            ResponseVo<List<SpuEntity>> spuResonseVo = this.gmallPmsClient.querySpuByPageJson(pageParamVo);
            List<SpuEntity> spuEntityList = spuResonseVo.getData();

            spuEntityList.forEach(spuEntity -> {

                //2.获取spu下的所有sku
                ResponseVo<List<SkuEntity>> skuListResonseVo = this.gmallPmsClient.querySpuBysouId(spuEntity.getId());
                List<SkuEntity> skuEntityList = skuListResonseVo.getData();
                if (!CollectionUtils.isEmpty(skuEntityList)) {
                    //skuEntityList转化为goodsList集合
                    List<Goods> goodsList = skuEntityList.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        goods.setSkuId(skuEntity.getId());
                        goods.setDefaultImage(skuEntity.getDefaultImage());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setSubTitle(skuEntity.getSubtitle());
                        goods.setPrice(skuEntity.getPrice().doubleValue());

                        //3.根据品牌id查询品牌参数
                        ResponseVo<BrandEntity> brandEntityResponseVo = this.gmallPmsClient.queryBrandById(skuEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        goods.setBrandId(brandEntity.getId());
                        goods.setBrandName(brandEntity.getName());
                        goods.setLogo(brandEntity.getLogo());
                        goods.setCreateTime(spuEntity.getCreateTime());

                        //4.根据分类id查询分类参数
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = this.gmallPmsClient.queryCategoryById(skuEntity.getCatagoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        goods.setCategoryId(categoryEntity.getId());
                        goods.setCategoryName(categoryEntity.getName());

                        //5.根据skuId查询排序参数
                        ResponseVo<List<WareSkuEntity>> listResponseVo = this.gmallWmsClient.queryWareSkuBykuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntityList = listResponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntityList)){
                            goods.setSales(wareSkuEntityList.stream().map(WareSkuEntity::getSales).reduce((a ,b) ->a+b).get());
                            goods.setStock(wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()-wareSkuEntity.getStockLocked() > 0));
                        }
                        //6.1根据分类id和skuId查询搜索属性参数
                        ArrayList<SearchAttrValueVo> searchAttrs = new ArrayList<>();
                        ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.gmallPmsClient.querySearchSkuAttrValuesByCidAndSkuId(skuEntity.getCatagoryId(), skuEntity.getId());
                        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                            List<SearchAttrValueVo> searchAttrValueVoList = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                                return searchAttrValueVo;
                            }).collect(Collectors.toList());
                            searchAttrs.addAll(searchAttrValueVoList);

                        }
                        //6.2根据分类id和spuId查询搜索属性参数
                        ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = this.gmallPmsClient.querySearchSpuAttrValuesByCidAndSpuId(skuEntity.getCatagoryId(), spuEntity.getId());
                        List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                            List<SearchAttrValueVo> searchAttrValueVos = spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                                return searchAttrValueVo;
                            }).collect(Collectors.toList());
                            searchAttrs.addAll(searchAttrValueVos);
                        }
                        goods.setSearchAttrs(searchAttrs);


                        return goods;
                    }).collect(Collectors.toList());
                    this.repository.saveAll(goodsList);
                }
            });
            pageNum++;
            pageSize = pageParamVo.getPageSize();
        } while (pageSize == 10);
    }
}
