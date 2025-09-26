package top.kimwonjoon.trigger.http.admin;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IModelDao;
import top.kimwonjoon.infrastructure.dao.po.Model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName ModelAdminController
 * @Description 模型管理控制器
 * @Author @kimwonjoon
 * @Date 2025/1/27
 */
@Slf4j
@RestController
@RequestMapping("/admin/model")
@CrossOrigin(origins = "*")
public class ModelAdminController {

    @Resource
    private IModelDao modelDao;

    /**
     * 获取所有模型
     */
    @GetMapping("/list")
    public ResponseEntity<List<Model>> getAllModels() {
        try {
            List<Model> models = modelDao.queryAll();
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            log.error("获取模型列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ID获取模型
     */
    @GetMapping("/{id}")
    public ResponseEntity<Model> getModelById(@PathVariable Integer id) {
        try {
            Model model = modelDao.queryById(id);
            if (model != null) {
                return ResponseEntity.ok(model);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取模型失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据模型名称获取模型
     */
    @GetMapping("/name/{modelName}")
    public ResponseEntity<Model> getModelByName(@PathVariable String modelName) {
        try {
            Model model = modelDao.queryByModelName(modelName);
            if (model != null) {
                return ResponseEntity.ok(model);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据名称获取模型失败，名称: {}", modelName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建模型
     */
    @PostMapping
    public ResponseEntity<String> createModel(@RequestBody Model model) {
        try {
            model.setCreateTime(LocalDateTime.now());
            model.setUpdateTime(LocalDateTime.now());
            int result = modelDao.insert(model);
            if (result > 0) {
                return ResponseEntity.ok("模型创建成功");
            } else {
                return ResponseEntity.badRequest().body("模型创建失败");
            }
        } catch (Exception e) {
            log.error("创建模型失败", e);
            return ResponseEntity.internalServerError().body("创建模型失败: " + e.getMessage());
        }
    }

    /**
     * 更新模型
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateModel(@PathVariable Integer id, @RequestBody Model model) {
        try {
            model.setId(id);
            model.setUpdateTime(LocalDateTime.now());
            int result = modelDao.update(model);
            if (result > 0) {
                return ResponseEntity.ok("模型更新成功");
            } else {
                return ResponseEntity.badRequest().body("模型更新失败");
            }
        } catch (Exception e) {
            log.error("更新模型失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().body("更新模型失败: " + e.getMessage());
        }
    }

    /**
     * 删除模型
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteModel(@PathVariable Integer id) {
        try {
            int result = modelDao.deleteById(id);
            if (result > 0) {
                return ResponseEntity.ok("模型删除成功");
            } else {
                return ResponseEntity.badRequest().body("模型删除失败");
            }
        } catch (Exception e) {
            log.error("删除模型失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().body("删除模型失败: " + e.getMessage());
        }
    }
}
