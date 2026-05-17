package com.example.dm.mapper;

import com.example.dm.entity.Store;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StoreMapper {
    int insert(Store store);
    int update(Store store);
    int delete(Integer id);
    Store selectById(Integer id);
    List<Store> selectAll();
}
