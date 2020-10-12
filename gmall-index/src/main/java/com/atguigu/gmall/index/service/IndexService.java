package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.gmallannotation.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;
    public List<CategoryEntity> queryOneCategory() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategories(0L);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }

    //public final static String KEY_PREFIX = "index:actegory:";
    @GmallCache(prefix = "index:category:",timeout = 14400,random = 360,lock = "lock")
    public List<CategoryEntity> queryTwoCategoryWithSubs(Long pid) {

        /*String uuid = UUID.randomUUID().toString();

        //查询缓存，如果命中直接返回
        String value = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(value)){
            String categorys = redisTemplate.opsForValue().get(value);
            List<CategoryEntity> categoryEntities = JSON.parseArray(categorys, CategoryEntity.class);
            return categoryEntities;
        }
*/
        //没有命中远程调用接口查询
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoryLev2WithSubs(pid);

        List<CategoryEntity> categories = listResponseVo.getData();
       /* // 把查询结果放入缓存
        if (CollectionUtils.isEmpty(categories)){

            this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categories), 5, TimeUnit.MINUTES);
        }else {
            this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categories), 30 + new Random().nextInt(5), TimeUnit.DAYS);
        }
        //存入缓存*/
        return categories;
    }
}
