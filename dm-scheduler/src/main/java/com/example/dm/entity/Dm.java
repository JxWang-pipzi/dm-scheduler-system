package com.example.dm.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Dm {
    private Integer id;
    private Integer userId;
    private Integer dmLevel;
    private Integer experience;
    private Double rating;
    private String status;
    /** 擅长类型，如：推理,恐怖,机制 */
    private String specialty;
    /** 累计主持场次 */
    private Integer totalSessions;
    /** 每周最大排班次数 */
    private Integer weeklyMaxSessions;
    /** 自我介绍 */
    private String bio;
    private Date createdAt;
    private Date updatedAt;
}