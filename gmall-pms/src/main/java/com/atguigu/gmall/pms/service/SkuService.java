package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sku信息
 *
 * @author qiangge
 * @email 23884280@qq.com
 * @date 2020-09-21 19:24:55
 */
public interface SkuService extends IService<SkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

