package com.atguigu.gmall.search.service;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchParamVo;
import com.atguigu.gmall.search.entity.SearchResponseAttrValueVo;
import com.atguigu.gmall.search.entity.SearchResponseVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//localhost:18086/search?keyword=手机&brandId=1,2,3&cid3=225,250&props=9:5-6-7&sort=1&priceFrom=1000&priceTo=8000&store=true&pageNum=1
@Service
public class SearchService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RestHighLevelClient client;


    public SearchResponseVo search(SearchParamVo paramVo) {
        SearchSourceBuilder source = buildDsl(paramVo);
        SearchRequest serachRequset = new SearchRequest(new String[]{"goods"}, source);

        try {
            SearchResponse search = client.search(serachRequset, RequestOptions.DEFAULT);

            SearchResponseVo responseVo = parseResult(search);
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());
            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //查询数据
    private SearchSourceBuilder buildDsl(SearchParamVo paramVo) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if (StringUtils.isBlank(paramVo.getKeyword())) {
            //TODO 打广告
            return sourceBuilder;
        }
        //1构建搜索条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1封装条件查询
        boolQueryBuilder.must(new MatchQueryBuilder("title", paramVo.getKeyword()).operator(Operator.AND));
        //1.2构建过滤条件
        //1.2.1构建品牌过滤
        List<Long> brandId = paramVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }

        //1.2.2构建分类过滤
        List<Long> cid3 = paramVo.getCid3();
        if (CollectionUtils.isNotEmpty(cid3)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", cid3));
        }

        //1.2.3构建价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
            if (priceFrom != null) {
                boolQueryBuilder.filter(rangeQueryBuilder.gte(priceFrom));
            }
            if (priceTo != null) {
                boolQueryBuilder.filter(rangeQueryBuilder.lte(priceTo));
            }
        }
        //1.2.4构建库存过滤
        Boolean store = paramVo.getStore();
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }
        //1.2.5规格参数嵌套过滤
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                BoolQueryBuilder nestedBoolQueryBuilder = QueryBuilders.boolQuery();
                //分割props  (props=4:8G-12G&props=5:128G-256G-521)
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2) {
                    nestedBoolQueryBuilder.must(QueryBuilders.termQuery("searchAttrs.attrId", attr[0]));
                    String[] attrValues = StringUtils.split(attr[1], "-");
                    nestedBoolQueryBuilder.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                }
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", nestedBoolQueryBuilder, ScoreMode.None));
            });
        }
        //2.构建排序条件
        Integer sort = paramVo.getSort();
        if (sort !=null){
        switch (sort) {
            case 1:
                sourceBuilder.sort("price", SortOrder.ASC);
            case 2:
                sourceBuilder.sort("price", SortOrder.DESC);
            case 3:
                sourceBuilder.sort("createTime", SortOrder.DESC);
            case 4:
                sourceBuilder.sort("sales", SortOrder.DESC);
            default:
                sourceBuilder.sort("_score", SortOrder.DESC);
        }
        }

        //3.构建分页条件
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        //4.构建高亮查询
        sourceBuilder.highlighter(new HighlightBuilder()
                .field("title")
                .preTags("<font style='color:red;'>")
                .postTags("</font>"));

        //5.构建聚合

        //5.1品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo"))
        );
        // 5.2分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("caterotyIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("caterotyNameAgg").field("categoryName")));

        //5.3搜索条件嵌套聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));
        System.out.println(sourceBuilder);

        // 6. 结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId", "title", "subTitle", "price", "defaultImage"}, null);
        return sourceBuilder;
    }
    //解析数据
    private SearchResponseVo parseResult(SearchResponse searchResponse){
        SearchResponseVo responseVo = new SearchResponseVo();
        //1.获取总命中数
        long totalHits = searchResponse.getHits().getTotalHits();
        responseVo.setTotal(totalHits);

        //2.解析分页数据
        //获取当前页的数据
        SearchHit[] hits = searchResponse.getHits().getHits();
        List<Goods> goodsList = Stream.of(hits).map(hit -> {
            try {
                //从命中数据中或者source中数据转换成Goods对象
                String sourceAsString = hit.getSourceAsString();
                Goods goods = MAPPER.readValue(sourceAsString, Goods.class);
                //获取高亮字段赋值给goods的title字段
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField title = highlightFields.get("title");
                Text[] fragments = title.getFragments();
                goods.setTitle(fragments[0].string());
                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);

        //3.解释聚合结果集
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();

        //3.1解析品牌聚合结果集
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        if (CollectionUtils.isNotEmpty(buckets)){
            List<BrandEntity> brandEntityList = buckets.stream().map(bucket -> {

                BrandEntity brandEntity = new BrandEntity();
                //取出桶中的key赋值给品牌的id
                long brandId = ((Terms.Bucket)bucket).getKeyAsNumber().longValue();
                brandEntity.setId(brandId);

                //取出品牌id中的子桶(品牌名称桶和品牌值)
                Map<String, Aggregation> subAggregationMap = ((Terms.Bucket)bucket).getAggregations().asMap();
                //取出品牌名称
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) subAggregationMap.get("brandNameAgg");
                List<? extends Terms.Bucket> brandNameBucket = brandNameAgg.getBuckets();
                if (CollectionUtils.isNotEmpty(brandNameBucket)) {
                    String brandName = brandNameBucket.get(0).getKeyAsString();
                    brandEntity.setName(brandName);
                }

                //取出品牌logo值
                ParsedStringTerms logosubAggs = (ParsedStringTerms) subAggregationMap.get("logoAgg");
                List<? extends Terms.Bucket> logoBucket = logosubAggs.getBuckets();
                if (CollectionUtils.isNotEmpty(logoBucket)) {
                    brandEntity.setLogo(logoBucket.get(0).getKeyAsString());
                }
                return brandEntity;

            }).collect(Collectors.toList());
            responseVo.setBrands(brandEntityList);
        }

        //3.2解析分类聚合结果集
        ParsedLongTerms caterotyIdAgg = (ParsedLongTerms) searchResponse.getAggregations().get("caterotyIdAgg");
        List<? extends Terms.Bucket> catetyIdBucket = caterotyIdAgg.getBuckets();
        if (CollectionUtils.isNotEmpty(catetyIdBucket)){
            List<CategoryEntity> categoryEntityList = catetyIdBucket.stream().map(bucket -> {
                //将分类的id和名称封装到categoryEntity；
                CategoryEntity categoryEntity = new CategoryEntity();
                //从桶中拿分类的id
                long categoryId = bucket.getKeyAsNumber().longValue();
                categoryEntity.setId(categoryId);
                //从子桶中拿分类名称
                ParsedStringTerms caterotyNameAgg = (ParsedStringTerms) bucket.getAggregations().get("caterotyNameAgg");
                List<? extends Terms.Bucket> cateroryBukect = caterotyNameAgg.getBuckets();
                if (CollectionUtils.isNotEmpty(cateroryBukect)) {
                    String catetoryName = cateroryBukect.get(0).getKeyAsString();
                    categoryEntity.setName(catetoryName);
                }

                return categoryEntity;
            }).collect(Collectors.toList());
            responseVo.setCategories(categoryEntityList);
        }

        //3.3解析规格参数解析结果集
        ParsedNested attrAgg = (ParsedNested) searchResponse.getAggregations().get("attrAgg");
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdBucket = attrIdAgg.getBuckets();
        if (CollectionUtils.isNotEmpty(attrIdBucket)){
            List<SearchResponseAttrValueVo> filters = attrIdBucket.stream().map(bucket -> {
                SearchResponseAttrValueVo searchResponseAttrValueVo = new SearchResponseAttrValueVo();
                //设置规格参数Id
                long attrId = bucket.getKeyAsNumber().longValue();
                searchResponseAttrValueVo.setAttrId(attrId);
                //取出子桶中名称和值的map集合桶
                Map<String, Aggregation> aggIdAggMap = bucket.getAggregations().asMap();
                //取出名称桶的聚合
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) aggIdAggMap.get("attrNameAgg");
                List<? extends Terms.Bucket> attrNameAggBuckets = attrNameAgg.getBuckets();
                if (CollectionUtils.isNotEmpty(attrNameAggBuckets)) {
                    //赋值名称
                    String attrName = attrNameAggBuckets.get(0).getKeyAsString();
                    searchResponseAttrValueVo.setAttrName(attrName);
                }
                //取出值的桶
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) aggIdAggMap.get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueBucket = attrValueAgg.getBuckets();
                if (CollectionUtils.isNotEmpty(attrValueBucket)) {
                    //给规格参数赋值
                    List<String> AttrValueList = attrValueBucket.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrValueVo.setAttrValues(AttrValueList);
                }

                return searchResponseAttrValueVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(filters);
        }
        return responseVo;
    }
}
