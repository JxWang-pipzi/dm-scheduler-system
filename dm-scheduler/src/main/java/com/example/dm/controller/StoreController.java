package com.example.dm.controller;

import com.example.dm.entity.Store;
import com.example.dm.service.StoreService;
import com.example.dm.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
public class StoreController {
    @Autowired
    private StoreService storeService;

    @PostMapping
    public Result createStore(@RequestBody Store store) {
        boolean success = storeService.createStore(store);
        return success ? Result.success() : Result.error("创建门店失败");
    }

    @PutMapping
    public Result updateStore(@RequestBody Store store) {
        boolean success = storeService.updateStore(store);
        return success ? Result.success() : Result.error("更新门店失败");
    }

    @DeleteMapping("/{id}")
    public Result deleteStore(@PathVariable Integer id) {
        boolean success = storeService.deleteStore(id);
        return success ? Result.success() : Result.error("删除门店失败");
    }

    @GetMapping("/{id}")
    public Result getStoreById(@PathVariable Integer id) {
        Store store = storeService.getStoreById(id);
        return store != null ? Result.success(store) : Result.error("门店不存在");
    }

    @GetMapping
    public Result getAllStores() {
        List<Store> stores = storeService.getAllStores();
        return Result.success(stores);
    }
}
