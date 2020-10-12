package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CommentReplayEntity;
import com.atguigu.gmall.pms.mapper.CommentReplayMapper;
import com.atguigu.gmall.pms.service.CommentReplayService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service("commentReplayService")
public class CommentReplayServiceImpl extends ServiceImpl<CommentReplayMapper, CommentReplayEntity> implements CommentReplayService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CommentReplayEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CommentReplayEntity>()
        );

        return new PageResultVo(page);
    }

}