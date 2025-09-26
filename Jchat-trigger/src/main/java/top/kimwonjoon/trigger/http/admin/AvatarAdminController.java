package top.kimwonjoon.trigger.http.admin;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAvatarDao;
import top.kimwonjoon.infrastructure.dao.po.Avatar;

import java.util.Date;
import java.util.List;

/**
 * @ClassName AvatarAdminController
 * @Description Avatar管理控制器
 * @Author @kimwonjoon
 * @Date 2025/9/26 16:52
 */
@Slf4j
@RestController
@RequestMapping("/admin/avatar")
@CrossOrigin(origins = "*")
public class AvatarAdminController {

    @Resource
    private IAvatarDao avatarDao;

    /**
     * 获取所有Avatar
     */
    @GetMapping("/list")
    public ResponseEntity<List<Avatar>> getAllAvatars() {
        try {
            List<Avatar> avatars = avatarDao.queryAll();
            return ResponseEntity.ok(avatars);
        } catch (Exception e) {
            log.error("获取Avatar列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ID获取Avatar
     */
    @GetMapping("/{id}")
    public ResponseEntity<Avatar> getAvatarById(@PathVariable Integer id) {
        try {
            Avatar avatar = avatarDao.queryById(id);
            if (avatar != null) {
                return ResponseEntity.ok(avatar);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取Avatar失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建Avatar
     */
    @PostMapping
    public ResponseEntity<String> createAvatar(@RequestBody Avatar avatar) {
        try {
            avatar.setCreateTime(new Date());
            avatar.setUpdateTime(new Date());
            int result = avatarDao.insert(avatar);
            if (result > 0) {
                return ResponseEntity.ok("Avatar创建成功");
            } else {
                return ResponseEntity.badRequest().body("Avatar创建失败");
            }
        } catch (Exception e) {
            log.error("创建Avatar失败", e);
            return ResponseEntity.internalServerError().body("创建Avatar失败: " + e.getMessage());
        }
    }

    /**
     * 更新Avatar
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateAvatar(@PathVariable Integer id, @RequestBody Avatar avatar) {
        try {
            avatar.setId(id);
            avatar.setUpdateTime(new Date());
            int result = avatarDao.update(avatar);
            if (result > 0) {
                return ResponseEntity.ok("Avatar更新成功");
            } else {
                return ResponseEntity.badRequest().body("Avatar更新失败");
            }
        } catch (Exception e) {
            log.error("更新Avatar失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().body("更新Avatar失败: " + e.getMessage());
        }
    }

    /**
     * 删除Avatar
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAvatar(@PathVariable Integer id) {
        try {
            int result = avatarDao.deleteById(id);
            if (result > 0) {
                return ResponseEntity.ok("Avatar删除成功");
            } else {
                return ResponseEntity.badRequest().body("Avatar删除失败");
            }
        } catch (Exception e) {
            log.error("删除Avatar失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().body("删除Avatar失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类获取Avatar
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Avatar>> getAvatarsByCategory(@PathVariable String category) {
        try {
            List<Avatar> avatars = avatarDao.queryByCategory(category);
            return ResponseEntity.ok(avatars);
        } catch (Exception e) {
            log.error("根据分类获取Avatar失败，分类: {}", category, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
