package com.example.doubanmoviecrawler.service;

import com.example.doubanmoviecrawler.Model.Movie;
import com.example.doubanmoviecrawler.dto.ExcelMovieDTO;
import com.example.doubanmoviecrawler.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 电影服务层
 * <p>
 * 提供电影数据的业务逻辑处理，包括保存、查询、导出等操作
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    /**
     * 保存电影信息到数据库
     * <p>
     * 如果电影名称已存在，则跳过保存
     * </p>
     *
     * @param movie 电影对象
     * @return 保存后的电影对象
     */
    @Transactional
    public Movie save(Movie movie) {
        if (movieRepository.existsByName(movie.getName())) {
            log.info("电影已存在，跳过保存: {}", movie.getName());
            return movie;
        }
        Movie savedMovie = movieRepository.save(movie);
        log.info("电影保存成功: {}", savedMovie.getName());
        return savedMovie;
    }

    /**
     * 批量保存电影信息到数据库
     * <p>
     * 自动跳过已存在的电影
     * </p>
     *
     * @param movies 电影列表
     * @return 实际保存的电影数量
     */
    @Transactional
    public int saveAll(List<Movie> movies) {
        int savedCount = 0;
        for (Movie movie : movies) {
            if (!movieRepository.existsByName(movie.getName())) {
                movieRepository.save(movie);
                savedCount++;
            }
        }
        log.info("批量保存完成，共保存 {} 部电影", savedCount);
        return savedCount;
    }

    /**
     * 查询所有电影
     *
     * @return 所有电影列表
     */
    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    /**
     * 根据ID查询电影
     *
     * @param id 电影ID
     * @return 电影对象
     */
    public Movie findById(Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    /**
     * 删除所有电影数据
     */
    @Transactional
    public void deleteAll() {
        movieRepository.deleteAll();
        log.info("已清空所有电影数据");
    }

    /**
     * 获取所有电影数据用于Excel导出
     * <p>
     * 将Movie实体转换为ExcelMovieDTO
     * </p>
     *
     * @return Excel导出用的电影数据列表
     */
    public List<ExcelMovieDTO> getAllForExport() {
        List<Movie> movies = movieRepository.findAll();
        log.info("准备导出 {} 部电影数据到Excel", movies.size());
        
        return movies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将Movie实体转换为ExcelMovieDTO
     *
     * @param movie Movie实体
     * @return ExcelMovieDTO
     */
    private ExcelMovieDTO convertToDTO(Movie movie) {
        ExcelMovieDTO dto = new ExcelMovieDTO();
        dto.setId(movie.getId() != null ? movie.getId().intValue() : null);
        dto.setName(movie.getName());
        dto.setScore(movie.getScore());
        dto.setUrl(movie.getUrl());
        return dto;
    }
}
