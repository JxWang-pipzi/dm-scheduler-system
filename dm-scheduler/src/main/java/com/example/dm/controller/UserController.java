package com.example.dm.controller;

import com.example.dm.entity.User;
import com.example.dm.service.UserService;
import com.example.dm.util.Result;
import com.example.dm.util.JwtUtil;
import com.example.dm.vo.PageResult;
import com.example.dm.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                return Result.error(4000, "用户名或密码不能为空");
            }
            User user = userService.login(username, password);
            if (user == null) {
                return Result.error(4001, "用户名或密码错误");
            }
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("user", toVO(user));
            data.put("token", JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole()));
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(5000, "登录失败");
        }
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty())
                return Result.error(4000, "用户名不能为空");
            if (user.getPassword() == null || user.getPassword().trim().isEmpty())
                return Result.error(4000, "密码不能为空");
            if (userService.getUserByUsername(user.getUsername()) != null)
                return Result.error(4002, "用户名已存在");
            String phoneError = validatePhone(user.getPhone(), true);
            if (phoneError != null) {
                return Result.error(4000, phoneError);
            }
            user.setPhone(normalizePhone(user.getPhone()));

            String role = user.getRole();
            if (role == null || role.trim().isEmpty()) {
                role = "USER";
            } else {
                role = role.toUpperCase();
            }
            if (!"USER".equals(role) && !"DM".equals(role)) {
                return Result.error(4000, "注册角色不合法");
            }
            user.setRole(role);

            User created = userService.addUser(user);
            if (created == null)
                return Result.error(5000, "注册失败");
            return Result.success(toVO(created));
        } catch (Exception e) {
            return Result.error(5000, "注册失败");
        }
    }

    @GetMapping("/list")
    public Result getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserVO> vos = users.stream().map(this::toVO).collect(Collectors.toList());
            return Result.success(vos);
        } catch (Exception e) {
            return Result.error(5000, "获取用户列表失败");
        }
    }

    /** 分页查询用户 */
    @GetMapping("/page")
    public Result getUserPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        try {
            PageResult<User> page = userService.getUserPage(keyword, role, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(5000, "获取用户列表失败");
        }
    }

    @GetMapping("/detail/{id}")
    public Result getUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            return user != null ? Result.success(toVO(user)) : Result.error(4004, "用户不存在");
        } catch (Exception e) {
            return Result.error(5000, "获取用户详情失败");
        }
    }

    @PostMapping("/add")
    public Result addUser(@RequestBody User user) {
        try {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty())
                return Result.error(4000, "用户名不能为空");
            if (user.getPassword() == null || user.getPassword().trim().isEmpty())
                return Result.error(4000, "密码不能为空");
            if (user.getRole() == null || user.getRole().trim().isEmpty())
                return Result.error(4000, "角色不能为空");
            if (userService.getUserByUsername(user.getUsername()) != null)
                return Result.error(4002, "用户名已存在");
            String phoneError = validatePhone(user.getPhone(), false);
            if (phoneError != null) {
                return Result.error(4000, phoneError);
            }
            user.setPhone(normalizePhone(user.getPhone()));
            User created = userService.addUser(user);
            if (created == null)
                return Result.error(5000, "添加用户失败");
            return Result.success(toVO(created));
        } catch (Exception e) {
            return Result.error(5000, "添加用户失败");
        }
    }

    @PutMapping("/update")
    public Result updateUser(@RequestBody User user) {
        try {
            if (user.getId() == null)
                return Result.error(4000, "id不能为空");
            User existing = userService.getUserById(user.getId());
            if (existing == null)
                return Result.error(4004, "用户不存在");
            if (user.getUsername() != null && !user.getUsername().equals(existing.getUsername())) {
                User u = userService.getUserByUsername(user.getUsername());
                if (u != null && !u.getId().equals(user.getId()))
                    return Result.error(4002, "用户名已存在");
            }
            if (user.getPhone() != null) {
                String phoneError = validatePhone(user.getPhone(), false);
                if (phoneError != null) {
                    return Result.error(4000, phoneError);
                }
                user.setPhone(normalizePhone(user.getPhone()));
            }
            User updated = userService.updateUser(user);
            if (updated == null)
                return Result.error(5000, "更新用户失败");
            return Result.success(toVO(updated));
        } catch (Exception e) {
            return Result.error(5000, "更新用户失败");
        }
    }

    @PutMapping("/password")
    public Result updatePassword(@RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, String> params) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> claims = JwtUtil.parseToken(token);
            if (claims == null) {
                return Result.error(4010, "请先登录");
            }
            Integer userId = (Integer) claims.get("userId");
            if (userId == null) {
                return Result.error(4011, "无效的凭证");
            }

            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            if (oldPassword == null || oldPassword.trim().isEmpty() || newPassword == null
                    || newPassword.trim().isEmpty()) {
                return Result.error(4000, "密码不能为空");
            }

            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error(4004, "用户不存在");
            }

            if (!user.getPassword().equals(oldPassword)) {
                return Result.error(4001, "原密码错误");
            }

            user.setPassword(newPassword);
            User updated = userService.updateUser(user);
            if (updated == null) {
                return Result.error(5000, "密码修改失败");
            }
            return Result.success("密码修改成功，请重新登录");
        } catch (Exception e) {
            return Result.error(5000, "密码修改失败");
        }
    }

    @GetMapping("/current")
    public Result getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Integer userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error(4010, "请先登录");
            }

            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error(4004, "用户不存在");
            }
            return Result.success(toVO(user));
        } catch (Exception e) {
            return Result.error(5000, "获取当前用户失败");
        }
    }

    @PutMapping("/profile")
    public Result updateProfile(@RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, String> params) {
        try {
            Integer userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error(4010, "请先登录");
            }

            User existing = userService.getUserById(userId);
            if (existing == null) {
                return Result.error(4004, "用户不存在");
            }

            String username = params.get("username");
            if (username != null && !username.trim().isEmpty() && !username.equals(existing.getUsername())) {
                User byUsername = userService.getUserByUsername(username);
                if (byUsername != null && !byUsername.getId().equals(userId)) {
                    return Result.error(4002, "用户名已存在");
                }
                existing.setUsername(username.trim());
            }
            if (params.containsKey("phone")) {
                String phone = params.get("phone");
                String phoneError = validatePhone(phone, false);
                if (phoneError != null) {
                    return Result.error(4000, phoneError);
                }
                existing.setPhone(normalizePhone(phone));
            }
            if (params.containsKey("realName")) {
                existing.setRealName(params.get("realName"));
            }
            if (params.containsKey("avatar")) {
                existing.setAvatar(params.get("avatar"));
            }

            User updated = userService.updateUser(existing);
            if (updated == null) {
                return Result.error(5000, "更新个人信息失败");
            }
            return Result.success(toVO(updated));
        } catch (Exception e) {
            return Result.error(5000, "更新个人信息失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteUser(@PathVariable Integer id) {
        try {
            boolean success = userService.deleteUser(id);
            return success ? Result.success() : Result.error("删除用户失败");
        } catch (Exception e) {
            return Result.error(5000, "删除用户失败");
        }
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole() == null ? null : user.getRole().toLowerCase());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    private Integer extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Map<String, Object> claims = JwtUtil.parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return (Integer) userId;
        }
        if (userId instanceof Number) {
            return ((Number) userId).intValue();
        }
        return null;
    }

    private String validatePhone(String phone, boolean required) {
        String normalized = normalizePhone(phone);
        if (normalized == null) {
            return required ? "手机号不能为空" : null;
        }
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            return "手机号必须为11位数字";
        }
        return null;
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String normalized = phone.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
