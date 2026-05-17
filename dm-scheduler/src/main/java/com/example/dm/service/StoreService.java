package com.example.dm.service;

import com.example.dm.entity.Store;

import java.util.List;

public interface StoreService {
    boolean createStore(Store store);
    boolean updateStore(Store store);
    boolean deleteStore(Integer id);
    Store getStoreById(Integer id);
    List<Store> getAllStores();
}
