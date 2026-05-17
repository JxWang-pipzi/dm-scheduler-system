package com.example.dm.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Reservation {
    private Integer id;
    private Integer userId;
    private Integer sessionId;
    /** 本次预约人数 */
    private Integer playersCount;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
