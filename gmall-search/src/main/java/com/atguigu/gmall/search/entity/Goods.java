package com.atguigu.gmall.search.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "goods",type = "info",shards = 3,replicas = 2)
public class Goods {
    //sku相关参数
    @Id
    @Field(type = FieldType.Long)
    private Long skuId;//sku的id
    @Field(type = FieldType.Keyword,index = false)
    private String defaultImage;//默认图片
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String title;//主标题
    @Field(type = FieldType.Keyword,index = false)
    private String subTitle;//副标题
    @Field(type = FieldType.Double)
    private Double price;//价格
    @Field(type = FieldType.Date)
    private Date createTime;//创建时间。可用于新品排序

    //品牌相关参数
    @Field(type = FieldType.Long)
    private Long brandId;//品牌id
    @Field(type = FieldType.Keyword)
    private String brandName;//品牌名称
    @Field(type = FieldType.Keyword)
    private String logo;//品牌logo

    //分类相关参数
    @Field(type = FieldType.Long)
    private Long categoryId;//分类的id
    @Field(type = FieldType.Keyword)
    private String categoryName;//分类的名称

    //搜索属性相关参数
    @Field(type = FieldType.Nested)
    private List<SearchAttrValueVo> searchAttrs;//搜索属性集合

    //排序相关参数
    @Field(type = FieldType.Long)
    private Long sales;//销量
    @Field(type = FieldType.Boolean)
    private Boolean stock = false;//是否有货
}
