package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;

import java.util.Map;

/**
 * 商品spu积分设置
 *
 * @author qiangge
 * @email 23884280@qq.com
 * @date 2020-09-21 20:06:36
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

