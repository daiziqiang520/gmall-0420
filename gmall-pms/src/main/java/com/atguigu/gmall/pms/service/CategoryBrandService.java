package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.CategoryBrandEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 品牌分类关联
 *
 * @author qiangge
 * @email 23884280@qq.com
 * @date 2020-09-21 19:24:55
 */
public interface CategoryBrandService extends IService<CategoryBrandEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

