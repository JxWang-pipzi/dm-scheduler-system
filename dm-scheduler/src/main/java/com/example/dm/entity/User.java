package com.example.dm.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String role;
    private String status;
    private String phone;
    private String realName;
    private String avatar;
    private Integer version;
    private Date createdAt;
    private Date updatedAt;
}
