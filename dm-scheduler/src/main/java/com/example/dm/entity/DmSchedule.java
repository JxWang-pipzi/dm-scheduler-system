package com.example.dm.entity;

import lombok.Data;
import java.util.Date;

/**
 * DM排班实体
 */
@Data
public class DmSchedule {
    private Integer id;
    /** DM编号 */
    private Integer dmId;
    /** 排班日期 */
    private Date scheduleDate;
    /** 时段：MORNING/AFTERNOON/EVENING/NIGHT */
    private String timeSlot;
    /** 关联场次ID（可为空） */
    private Integer sessionId;
    /** 状态：AVAILABLE/ASSIGNED/REST */
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
