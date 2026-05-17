package com.example.dm.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Session {
    private Integer id;
    private Integer scriptId;
    private Integer dmId;
    private Date startTime;
    private Date endTime;
    private String status;
    private Integer maxPlayers;
    private Integer currentPlayers;
    private Integer version;
    private Date createdAt;
    private Date updatedAt;
}