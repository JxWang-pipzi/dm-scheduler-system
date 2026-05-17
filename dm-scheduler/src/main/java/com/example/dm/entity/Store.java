package com.example.dm.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Store {
    private Integer id;
    private String storeName;
    private String address;
    private String phone;
    private String description;
    /** 包厢数量 */
    private Integer roomCount;
    /** 营业时间 */
    private String businessHours;
    /** 纬度 */
    private BigDecimal latitude;
    /** 经度 */
    private BigDecimal longitude;
    /** 门店状态：OPEN/CLOSED/MAINTENANCE */
    private String status;
    private Date createdAt;
}
