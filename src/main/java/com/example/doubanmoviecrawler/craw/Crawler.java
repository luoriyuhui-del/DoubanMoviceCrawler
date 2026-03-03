package com.example.doubanmoviecrawler.craw;

import com.example.doubanmoviecrawler.Model.Movie;
import com.example.doubanmoviecrawler.service.MovieService;
import com.example.doubanmoviecrawler.util.MovieParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 豆瓣电影爬虫核心类
 * <p>
 * 该类负责从豆瓣Top250电影榜单爬取电影数据
 * 使用Jsoup库进行HTML解析，支持分页爬取
 * 爬取的数据会自动保存到MySQL数据库
 * </p>
 *
 * @author example
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Crawler {

    /**
     * 电影解析器，用于解析HTML文档并提取电影信息
     */
    private final MovieParser movieParser;

    /**
     * 电影服务层，用于将电影数据持久化到数据库
     */
    private final MovieService movieService;

    /**
     * 豆瓣Top250电影榜单的基础URL
     */
    private static final String BASE_URL = "https://movie.douban.com/top250";

    /**
     * 模拟浏览器的User-Agent请求头
     * <p>
     * 设置User-Agent可以避免被网站识别为爬虫而拒绝访问
     * 这里模拟的是Chrome 120浏览器
     * </p>
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * 需要爬取的总页数
     * <p>
     * 豆瓣Top250共250部电影，每页25部，共10页
     * </p>
     */
    private static final int TOTAL_PAGES = 10;

    /**
     * 每页显示的电影数量
     */
    private static final int PAGE_SIZE = 25;

    /**
     * 请求间隔时间（毫秒）
     * <p>
     * 在两次请求之间添加延迟，避免请求过于频繁被封禁IP
     * 建议设置为1000ms以上
     * </p>
     */
    private static final int REQUEST_DELAY_MS = 10000;

    /**
     * 爬虫入口方法，开始爬取豆瓣Top250电影数据
     * <p>
     * 该方法会遍历所有页面，逐页爬取电影信息
     * 爬取完成后会自动将数据保存到MySQL数据库
     * 爬取过程中会记录详细日志，包括每页的爬取状态和最终结果
     * </p>
     *
     * @return 包含所有爬取到的电影信息的List集合
     *         如果某页爬取失败，该页的电影数据将被跳过，不会影响其他页面的爬取
     */
    public List<Movie> getStart() {
        log.info("开始爬取豆瓣Top250电影数据...");

        // 用于存储所有爬取到的电影数据
        List<Movie> allMovies = new ArrayList<>();

        // 遍历所有页面进行爬取
        for (int page = 0; page < TOTAL_PAGES; page++) {
            // 计算当前页的起始位置（豆瓣分页参数）
            // 第1页: start=0, 第2页: start=25, 第3页: start=50, 以此类推
            int start = page * PAGE_SIZE;

            // 构建完整的请求URL
            String url = BASE_URL + "?start=" + start;

            try {
                log.info("正在爬取第 {} 页: {}", page + 1, url);

                // 使用Jsoup发送HTTP GET请求并获取HTML文档
                // userAgent(): 设置请求头，模拟浏览器访问
                // timeout(): 设置超时时间为20秒，避免请求卡死
                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(20000)
                        .get();

                // 调用MovieParser解析HTML文档，提取电影信息
                List<Movie> movies = movieParser.parse(doc);

                // 将当前页的电影数据添加到总列表中
                allMovies.addAll(movies);

                log.info("第 {} 页爬取完成，获取 {} 部电影", page + 1, movies.size());

                // 如果不是最后一页，则休眠一段时间再继续请求
                // 这样可以避免请求过于频繁被封禁IP
                if (page < TOTAL_PAGES - 1) {
                    Thread.sleep(REQUEST_DELAY_MS);
                }

            } catch (IOException e) {
                // 处理网络IO异常，如连接超时、服务器拒绝等
                // 记录错误日志但继续爬取下一页
                log.error("爬取第 {} 页时发生IO异常: {}", page + 1, e.getMessage());
            } catch (InterruptedException e) {
                // 处理线程休眠被中断的异常
                // 恢复中断状态并记录日志
                log.error("线程休眠被中断: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        log.info("数据爬取完毕，共获取 {} 部电影", allMovies.size());

        // 将爬取的数据保存到数据库
        saveToDatabase(allMovies);

        return allMovies;
    }

    /**
     * 将电影数据保存到数据库
     * <p>
     * 调用MovieService进行数据持久化
     * 自动跳过已存在的电影（根据电影名称判断）
     * </p>
     *
     * @param movies 电影列表
     */
    private void saveToDatabase(List<Movie> movies) {
        log.info("开始保存电影数据到数据库...");
        int savedCount = movieService.saveAll(movies);
        log.info("数据库保存完成，新增 {} 部电影", savedCount);
    }
}
