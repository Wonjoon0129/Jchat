package top.kimwonjoon.trigger.http.admin;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IUserDao;
import top.kimwonjoon.infrastructure.dao.po.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName UserAdminController
 * @Description 用户管理控制器
 * @Author @kimwonjoon
 * @Date 2025/1/27
 */
@Slf4j
@RestController
@RequestMapping("/admin/user")
@CrossOrigin(origins = "*")
public class UserAdminController {

    @Resource
    private IUserDao userDao;

    /**
     * 获取所有用户
     */
    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userDao.queryAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        try {
            User user = userDao.queryById(id);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取用户失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据UUID获取用户
     */
    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<User> getUserByUuid(@PathVariable String uuid) {
        try {
            User user = userDao.queryByUuid(uuid);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据UUID获取用户失败，UUID: {}", uuid, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            int result = userDao.insert(user);
            if (result > 0) {
                return ResponseEntity.ok("用户创建成功");
            } else {
                return ResponseEntity.badRequest().body("用户创建失败");
            }
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return ResponseEntity.internalServerError().body("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Integer id, @RequestBody User user) {
        try {
            user.setId(id);
            user.setUpdateTime(LocalDateTime.now());
            int result = userDao.update(user);
            if (result > 0) {
                return ResponseEntity.ok("用户更新成功");
            } else {
                return ResponseEntity.badRequest().body("用户更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().body("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        try {
            int result = userDao.deleteById(id);
            if (result > 0) {
                return ResponseEntity.ok("用户删除成功");
            } else {
                return ResponseEntity.badRequest().body("用户删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().body("删除用户失败: " + e.getMessage());
        }
    }
}
