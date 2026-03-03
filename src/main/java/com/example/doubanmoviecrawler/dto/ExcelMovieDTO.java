package com.example.doubanmoviecrawler.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * 电影Excel导出DTO类
 * <p>
 * 用于将电影数据映射到Excel表格的列
 * </p>
 */
@Data
@HeadRowHeight(20)
public class ExcelMovieDTO {

    @ExcelProperty(value = "序号", index = 0)
    @ColumnWidth(10)
    private Integer id;

    @ExcelProperty(value = "电影名称", index = 1)
    @ColumnWidth(40)
    private String name;

    @ExcelProperty(value = "评分", index = 2)
    @ColumnWidth(10)
    private Double score;

    @ExcelProperty(value = "豆瓣链接", index = 3)
    @ColumnWidth(60)
    private String url;
}
