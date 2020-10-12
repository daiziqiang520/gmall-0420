package com.atguigu.gmall.search.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class SearchAttrValueVo {
    @Field(type = FieldType.Long)
    private Long attrId;//属性id
    @Field(type = FieldType.Keyword)
    private String attrName;//属性名称
    @Field(type = FieldType.Keyword)
    private String attrValue;//属性值
}
