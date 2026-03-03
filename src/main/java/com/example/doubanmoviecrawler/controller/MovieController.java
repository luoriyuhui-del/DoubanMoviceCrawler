package com.example.doubanmoviecrawler.controller;

import com.alibaba.excel.EasyExcel;
import com.example.doubanmoviecrawler.Model.Movie;
import com.example.doubanmoviecrawler.craw.Crawler;
import com.example.doubanmoviecrawler.dto.ExcelMovieDTO;
import com.example.doubanmoviecrawler.service.MovieService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 电影控制器
 * <p>
 * 提供电影爬取、查询和导出的REST API接口
 * </p>
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final Crawler crawler;
    private final MovieService movieService;
    private final com.example.doubanmoviecrawler.craw.AdvancedCrawler advancedCrawler;

    /**
     * 触发爬虫任务（基础版）
     * <p>
     * 启动豆瓣 Top250 电影爬取任务，并将数据保存到数据库
     * </p>
     *
     * @return 爬取结果
     */
    @PostMapping("/crawl")
    public ResponseEntity<Map<String, Object>> startCrawl() {
        List<Movie> movies = crawler.getStart();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "爬取完成（基础版）");
        result.put("total", movies.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 触发爬虫任务（高级版 - 带反爬策略）
     * <p>
     * 使用高级反爬策略爬取豆瓣 Top250 电影数据
     * 特性：随机 User-Agent、随机延迟、自动重试、状态码检测
     * </p>
     *
     * @return 爬取结果
     */
    @PostMapping("/crawl/advanced")
    public ResponseEntity<Map<String, Object>> startAdvancedCrawl() {
        List<Movie> movies = advancedCrawler.crawl();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "爬取完成（高级版）");
        result.put("total", movies.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 查询所有电影
     *
     * @return 电影列表
     */
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movies = movieService.findAll();
        return ResponseEntity.ok(movies);
    }

    /**
     * 根据ID查询电影
     *
     * @param id 电影ID
     * @return 电影信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        Movie movie = movieService.findById(id);
        if (movie == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(movie);
    }

    /**
     * 导出电影数据到Excel文件
     * <p>
     * 将数据库中的所有电影数据导出为Excel文件，支持浏览器直接下载
     * 文件名格式：豆瓣电影Top250_时间戳.xlsx
     * </p>
     *
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        
        // 设置文件名（中文需要URL编码）
        String fileName = URLEncoder.encode("豆瓣电影Top250", StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        
        // 获取导出数据
        List<ExcelMovieDTO> dataList = movieService.getAllForExport();
        
        // 使用EasyExcel导出
        EasyExcel.write(response.getOutputStream(), ExcelMovieDTO.class)
                .sheet("豆瓣电影Top250")
                .doWrite(dataList);
    }

    /**
     * 清空所有电影数据
     *
     * @return 操作结果
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteAllMovies() {
        movieService.deleteAll();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已清空所有电影数据");
        return ResponseEntity.ok(result);
    }
}
