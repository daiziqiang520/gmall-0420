package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author qiangge
 * @email 23884280@qq.com
 * @date 2020-09-21 20:46:57
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
