package com.example.dm.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Script {
    private Integer id;
    private String scriptName;
    private String type;
    private String difficulty;
    private Integer needDmLevel;
    /** 最少玩家数 */
    private Integer minPlayers;
    private Integer maxPlayers;
    /** 预计时长（分钟） */
    private Integer duration;
    /** 单人价格 */
    private BigDecimal price;
    /** 封面图片URL */
    private String coverImage;
    /** 标签，如：推理,恐怖,情感 */
    private String tags;
    private String description;
    private Date createdAt;
    private Date updatedAt;
}