package com.example.doubanmoviecrawler.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 电影实体类
 * <p>
 * 对应数据库表 movie，存储豆瓣电影信息
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movie")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "score", columnDefinition = "DECIMAL(3,1)")
    private Double score;

    @Column(name = "url", length = 500)
    private String url;

    public Movie(String name, Double score, String url) {
        this.name = name;
        this.score = score;
        this.url = url;
    }
}
