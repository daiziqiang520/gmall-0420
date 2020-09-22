package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.SkuEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    // 分页查询spu列表
    @Override
    public PageResultVo queryspuByCidPage(Long categoryId, PageParamVo pageParamVo) {

        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<SpuEntity>();
        //判断categoryId是否为0，如果是0怎检索所有
        if (categoryId !=0){
            wrapper.eq("category_id", categoryId);
        }

        //取出关键字
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.eq("id",key).or().like("name", key));
        }
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),wrapper
        );
        return new PageResultVo(page);
    }

}