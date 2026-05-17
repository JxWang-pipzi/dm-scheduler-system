package com.example.dm.entity;

import lombok.Data;
import java.util.Date;

/**
 * 操作日志实体
 */
@Data
public class OperationLog {
    private Integer id;
    /** 操作用户ID */
    private Integer userId;
    /** 操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT */
    private String action;
    /** 操作对象：USER/DM/SCRIPT/SESSION/ORDER等 */
    private String target;
    /** 操作详情 */
    private String detail;
    /** 操作IP */
    private String ip;
    private Date createdAt;
}
