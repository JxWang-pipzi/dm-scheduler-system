package com.example.dm.service.impl;

import com.example.dm.entity.Store;
import com.example.dm.mapper.StoreMapper;
import com.example.dm.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {
    @Autowired
    private StoreMapper storeMapper;

    @Override
    public boolean createStore(Store store) {
        return storeMapper.insert(store) > 0;
    }

    @Override
    public boolean updateStore(Store store) {
        return storeMapper.update(store) > 0;
    }

    @Override
    public boolean deleteStore(Integer id) {
        return storeMapper.delete(id) > 0;
    }

    @Override
    public Store getStoreById(Integer id) {
        return storeMapper.selectById(id);
    }

    @Override
    public List<Store> getAllStores() {
        return storeMapper.selectAll();
    }
}
