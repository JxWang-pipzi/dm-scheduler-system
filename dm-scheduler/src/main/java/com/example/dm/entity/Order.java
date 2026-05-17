package com.example.dm.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Order {
    private Integer id;
    /** 订单编号 */
    private String orderNo;
    private Integer userId;
    private Integer sessionId;
    /** 订单总价 */
    private BigDecimal totalPrice;
    /** 订单状态：PENDING/PAID/COMPLETED/CANCELLED/REFUNDED */
    private String status;
    /** 支付时间 */
    private Date payTime;
    /** 支付方式：WECHAT/ALIPAY/CASH */
    private String payMethod;
    /** 备注 */
    private String remark;
    private Date createTime;
    private Date updatedAt;
}