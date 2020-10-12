package listener;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.client.GmallPmsClient;
import com.atguigu.gmall.search.client.GmallWmsClient;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GoodListener {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GoodsRepository repository;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH_ADD_QUEUE",durable = "true"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listener(Long spuId, Channel channel, Message message){
        ResponseVo<List<SkuEntity>> skuListResonseVo = this.gmallPmsClient.querySpuBysouId(spuId);
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
                ResponseVo<SpuEntity> spuEntityResponseVo = this.gmallPmsClient.querySpuById(spuId);
                SpuEntity spuEntity = spuEntityResponseVo.getData();
                if (spuEntity !=null){
                    goods.setCreateTime(spuEntity.getCreateTime());
                }


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
                ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = this.gmallPmsClient.querySearchSpuAttrValuesByCidAndSpuId(skuEntity.getCatagoryId(), spuId);
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
    }
}
