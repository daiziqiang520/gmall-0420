package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author qiangge
 * @email 23884280@qq.com
 * @date 2020-09-21 19:24:54
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    //根据spuId查询当前sku下的销售属性与skuId的映射关系
    @GetMapping("queryskuAttrsByspuIdwithskuId/{spuId}")
    public ResponseVo<String> queryskuAttrsByspuIdwithskuId(@PathVariable("spuId")Long spuId){
      String josn =   this.skuAttrValueService.queryskuAttrsByspuIdwithskuId(spuId);
      return ResponseVo.ok(josn);
    }


    //根据spuId查询spu下所有sku所有的销售属性
    @GetMapping("querySkuSaleValuesBySpuId/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySkuSaleValuesBySpuId(@PathVariable("spuId")Long spuId){
        List<SaleAttrValueVo> saleAttrValueVos = this.skuAttrValueService.querySkuSaleValuesBySpuId(spuId);
        return ResponseVo.ok(saleAttrValueVos);
    }
    //根据skuId查询当前sku的销售属性
    @GetMapping("querySkuSaleValueBySkuId/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuSaleValueBySkuId(@PathVariable("skuId")Long skuId){
        List<SkuAttrValueEntity> skuAttrValueEntities= this.skuAttrValueService.querySkuSaleValueBySkuId(skuId);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    @PostMapping("search/{categoryId}/{skuId}")
    @ApiOperation("根据分类id和skuId查询搜索属性")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchSkuAttrValuesByCidAndSkuId(@PathVariable("categoryId")Long categoryId,
                                                                                      @PathVariable("skuId")Long skuId){
        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueService.querySearchSkuAttrValuesByCidAndSkuId(categoryId,skuId);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id){
		SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
