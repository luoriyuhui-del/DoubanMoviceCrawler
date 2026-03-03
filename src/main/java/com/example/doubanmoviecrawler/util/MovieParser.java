package com.example.doubanmoviecrawler.util;

import com.example.doubanmoviecrawler.Model.Movie;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MovieParser {

    private static final String ITEM = "div.item";
    private static final String TITLE = "span.title";
    private static final String RATING_NUM = "span.rating_num";
    private static final String A = "a";

    public List<Movie> parse(org.jsoup.nodes.Document doc) {
        return doc.select(ITEM).stream()
                .map(this::parseMovie)
                .filter(movie -> movie != null && movie.getName() != null && !movie.getName().isEmpty())
                .toList();
    }

    private Movie parseMovie(Element element) {
        try {
            String name = safeGetText(element, TITLE);
            if (name == null || name.isEmpty()) {
                return null;
            }

            String scoreStr = safeGetText(element, RATING_NUM);
            double score = parseScore(scoreStr);

            String url = safeGetAttr(element, A, "href");

            return new Movie(name, score, url);
        } catch (Exception e) {
            log.warn("解析电影信息时发生异常: {}", e.getMessage());
            return null;
        }
    }

    private String safeGetText(Element element, String selector) {
        Element selected = element.selectFirst(selector);
        return selected != null ? selected.text() : null;
    }

    private String safeGetAttr(Element element, String selector, String attr) {
        Element selected = element.selectFirst(selector);
        return selected != null ? selected.attr(attr) : null;
    }

    private double parseScore(String scoreStr) {
        if (scoreStr == null || scoreStr.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(scoreStr);
        } catch (NumberFormatException e) {
            log.warn("无法解析评分: {}", scoreStr);
            return 0.0;
        }
    }
}
