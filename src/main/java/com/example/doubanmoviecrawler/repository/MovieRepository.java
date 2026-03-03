package com.example.doubanmoviecrawler.repository;

import com.example.doubanmoviecrawler.Model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 电影数据访问层接口
 * <p>
 * 继承JpaRepository，提供基本的CRUD操作
 * </p>
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * 根据电影名称查询电影
     *
     * @param name 电影名称
     * @return 电影对象（可能为空）
     */
    Optional<Movie> findByName(String name);

    /**
     * 检查电影名称是否存在
     *
     * @param name 电影名称
     * @return 是否存在
     */
    boolean existsByName(String name);
}
