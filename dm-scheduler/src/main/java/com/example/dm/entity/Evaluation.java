package com.example.dm.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Evaluation {
    private Integer id;
    private Integer userId;
    private Integer sessionId;
    private Integer dmId;
    private Integer rating;
    private String comment;
    private Date createdAt;
}
