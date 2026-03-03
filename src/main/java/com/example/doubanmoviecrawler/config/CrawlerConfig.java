package com.example.doubanmoviecrawler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Data
@Component
@ConfigurationProperties(prefix = "crawler")
public class CrawlerConfig {

    private int minDelay = 2000;
    private int maxDelay = 5000;
    private int maxRetries = 3;
    private int timeout = 20000;
    
    private static final Random RANDOM = new Random();
    
    public Map<String, String> getHeaders() {
        return Map.of(
            "User-Agent", getRandomUserAgent(),
            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8",
            "Accept-Encoding", "gzip, deflate, br",
            "Connection", "keep-alive",
            "Upgrade-Insecure-Requests", "1",
            "Cache-Control", "max-age=0",
            "Referer", "https://www.douban.com/"
        );
    }
    
    private String getRandomUserAgent() {
        String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:121.0) Gecko/20100101 Firefox/121.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/120.0.0.0 Safari/537.36"
        };
        return userAgents[RANDOM.nextInt(userAgents.length)];
    }
    
    public int getRandomDelay() {
        return minDelay + RANDOM.nextInt(maxDelay - minDelay);
    }
}
