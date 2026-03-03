package com.example.doubanmoviecrawler.craw;

import com.example.doubanmoviecrawler.Model.Movie;
import com.example.doubanmoviecrawler.config.CrawlerConfig;
import com.example.doubanmoviecrawler.service.MovieService;
import com.example.doubanmoviecrawler.util.MovieParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdvancedCrawler {

    private final MovieParser movieParser;
    private final MovieService movieService;
    private final CrawlerConfig config;

    private static final String BASE_URL = "https://movie.douban.com/top250";
    private static final int TOTAL_PAGES = 10;
    private static final int PAGE_SIZE = 25;

    public List<Movie> crawl() {
        log.info("开始爬取豆瓣 Top250 电影数据（高级版）...");
        
        List<Movie> allMovies = new ArrayList<>();

        for (int page = 0; page < TOTAL_PAGES; page++) {
            int start = page * PAGE_SIZE;
            String url = BASE_URL + "?start=" + start;

            try {
                log.info("正在爬取第 {} 页：{}", page + 1, url);

                Document doc = fetchWithRetry(url, 0);
                
                if (doc != null) {
                    List<Movie> movies = movieParser.parse(doc);
                    allMovies.addAll(movies);
                    log.info("第 {} 页爬取完成，获取 {} 部电影", page + 1, movies.size());
                }

                if (page < TOTAL_PAGES - 1) {
                    int delay = config.getRandomDelay();
                    log.debug("等待 {} 毫秒后继续...", delay);
                    Thread.sleep(delay);
                }

            } catch (InterruptedException e) {
                log.error("线程休眠被中断：{}", e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("爬取第 {} 页时发生异常：{}", page + 1, e.getMessage());
            }
        }

        log.info("数据爬取完毕，共获取 {} 部电影", allMovies.size());
        saveToDatabase(allMovies);

        return allMovies;
    }

    private Document fetchWithRetry(String url, int retryCount) throws IOException, InterruptedException {
        try {
            Map<String, String> headers = config.getHeaders();
            
            Connection connection = Jsoup.connect(url)
                .timeout(config.getTimeout())
                .headers(headers)
                .followRedirects(false);
            
            Connection.Response response = connection.execute();
            
            int statusCode = response.statusCode();
            log.debug("请求状态码：{}", statusCode);
            
            if (statusCode == 301 || statusCode == 302) {
                String redirectUrl = response.header("Location");
                log.info("重定向到：{}", redirectUrl);
                return fetchWithRetry(redirectUrl, retryCount);
            }
            
            if (statusCode == 429) {
                log.warn("请求过于频繁，等待 60 秒后重试...");
                if (retryCount < config.getMaxRetries()) {
                    Thread.sleep(60000);
                    return fetchWithRetry(url, retryCount + 1);
                } else {
                    throw new IOException("达到最大重试次数，IP 可能已被限制");
                }
            }
            
            if (statusCode >= 400) {
                throw new IOException("HTTP 错误：" + statusCode);
            }
            
            return response.parse();
            
        } catch (IOException e) {
            if (retryCount < config.getMaxRetries()) {
                log.warn("请求失败，{} 秒后重试（第 {}/{} 次）", 
                    5 * (retryCount + 1), retryCount + 1, config.getMaxRetries());
                Thread.sleep(5000L * (retryCount + 1));
                return fetchWithRetry(url, retryCount + 1);
            }
            throw e;
        }
    }

    private void saveToDatabase(List<Movie> movies) {
        log.info("开始保存电影数据到数据库...");
        int savedCount = movieService.saveAll(movies);
        log.info("数据库保存完成，新增 {} 部电影", savedCount);
    }
}
