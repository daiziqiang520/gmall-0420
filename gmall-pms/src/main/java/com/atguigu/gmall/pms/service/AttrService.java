package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品属性
 *
 * @author qiangge
 * @email 23884280@qq.com
 * @date 2020-09-21 19:24:55
 */
public interface AttrService extends IService<AttrEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

}

