package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品三级分类
 *
 * @author qiangge
 * @email 23884280@qq.com
 * @date 2020-09-21 19:24:55
 */
@Api(tags = "商品三级分类 管理")
@RestController
@RequestMapping("pms/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //根据categoryId查询三级分类，在查询所有分类相关信息
    @GetMapping("queryAllCategories/{c3id}")
    public ResponseVo<List<CategoryEntity>> queryLev3CategoryAndLev2CategoryAndLev1CategoryByLev3CategoryId(@PathVariable("c3id")Long c3id){
        List<CategoryEntity> categoryEntities = this.categoryService.queryLev3CategoryAndLev2CategoryAndLev1CategoryByLev3CategoryId(c3id);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategories(@PathVariable("parentId")Long parentId){
       List<CategoryEntity> categories = this.categoryService.queryCategories(parentId);
       return ResponseVo.ok(categories);
    }

    @GetMapping("parent/withsub/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoryLev2WithSubs(@PathVariable("parentId") Long parentId){
       List<CategoryEntity> categoryEntities =  this.categoryService.queryCategoryLev2WithSubs(parentId);
       return ResponseVo.ok(categoryEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryCategoryByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = categoryService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id){
		CategoryEntity category = categoryService.getById(id);

        return ResponseVo.ok(category);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		categoryService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
