package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface GmallPmsApi {
    @PostMapping("pms/spu/json")
    @ApiOperation("分页查询·远程调用")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySpuBysouId(@PathVariable("spuId")Long spuId);
    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @PostMapping("pms/skuattrvalue/search/{categoryId}/{skuId}")
    @ApiOperation("根据分类id和skuId查询搜索属性")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchSkuAttrValuesByCidAndSkuId(@PathVariable("categoryId")Long categoryId,
                                                                                      @PathVariable("skuId")Long skuId);
    @PostMapping("pms/spuattrvalue/search/{categoryId}/{spuId}")
    @ApiOperation("根据分类id和spuId查询搜索属性")
    public ResponseVo<List<SpuAttrValueEntity>>querySearchSpuAttrValuesByCidAndSpuId(
            @PathVariable("categoryId")Long categoryId,
            @PathVariable("spuId")Long spuId
    );
    @GetMapping("pms/spu/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategories(@PathVariable("parentId")Long parentId);

    @GetMapping("pms/category/parent/withsub/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoryLev2WithSubs(@PathVariable("parentId") Long parentId);

    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    @GetMapping("pms/skuimages/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuImagesEntity> querySkuImagesById(@PathVariable("id") Long id);

    //根据skuId查询sku图片信息
    @GetMapping("pms/skuimages/querySkuImagesBySkuId/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId")Long skuId);
    //根据categoryId查询三级分类，在查询所有分类相关信息
    @GetMapping("pms/category/queryAllCategories/{c3id}")
    public ResponseVo<List<CategoryEntity>> queryLev3CategoryAndLev2CategoryAndLev1CategoryByLev3CategoryId(@PathVariable("c3id")Long c3id);

    //根据spuId查询spu下所有sku所有的销售属性
    @GetMapping("pms/skuattrvalue/querySkuSaleValuesBySpuId/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySkuSaleValuesBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/skuattrvalue/querySkuSaleValueBySkuId/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuSaleValueBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("pms/spudesc/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    //根据spuId查询当前sku下的销售属性与skuId的映射关系
    @GetMapping("pms/skuattrvalue/queryskuAttrsByspuIdwithskuId/{spuId}")
    public ResponseVo<String> queryskuAttrsByspuIdwithskuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/attrgroup/withattrvalues/{categoryId}")
    public ResponseVo<List<ItemGroupVo>> queryGroupsBySpuIdAndCid(@PathVariable("categoryId")Long categoryId,
                                                                  @RequestParam("skuId") Long skuId,
                                                                  @RequestParam("spuId") Long spuId);
}
