package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategories(Long parentId) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if (parentId != -1){
            wrapper.eq("parent_id", parentId);
        }
        List<CategoryEntity> categoies = this.list(wrapper);
        return categoies;
    }

    @Override
    public List<CategoryEntity> queryCategoryLev2WithSubs(Long parentId) {
        List<CategoryEntity> categoryEntities = this.categoryMapper.queryCategoryLev2WithSubs(parentId);
        return categoryEntities;
    }

    @Override
    public List<CategoryEntity> queryLev3CategoryAndLev2CategoryAndLev1CategoryByLev3CategoryId(Long c3id) {
        //查询3级分类
        CategoryEntity c3Entity = this.baseMapper.selectById(c3id);
        //查询2级分类
        CategoryEntity c2Entity = this.baseMapper.selectById(c3Entity.getParentId());
        //查询1级分类
        CategoryEntity c1Entity = this.baseMapper.selectById(c2Entity.getParentId());
        return Arrays.asList(c1Entity,c2Entity,c3Entity);
    }
}