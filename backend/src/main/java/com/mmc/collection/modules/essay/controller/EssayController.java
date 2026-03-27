package com.mmc.collection.modules.essay.controller;

import com.mmc.collection.modules.essay.entity.EssayDO;
import com.mmc.collection.modules.essay.entity.association.UserEssayDO;
import com.mmc.collection.modules.essay.service.EssayService;
import com.mmc.collection.modules.essay.service.association.UserEssayService;
import com.mmc.collection.modules.user.dao.UserDAO;
import com.mmc.collection.modules.user.entity.UserDO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(
        origins = "*",
        methods = {RequestMethod.GET, RequestMethod.POST,
                RequestMethod.PUT, RequestMethod.DELETE,
                RequestMethod.OPTIONS, RequestMethod.PATCH, RequestMethod.HEAD},
        allowedHeaders = "*"
)
@RestController
@RequestMapping("/api/essay")
@RequiredArgsConstructor
public class EssayController {

    private final EssayService essayService;
    private final UserDAO userDAO;
    private final UserEssayService userEssayService;

    /**
     * 获取所有文章
     * GET http://localhost:8080/api/essay
     */
    @GetMapping
    public List<EssayDO> getAllEssay() {
        return essayService.getAllEssay();
    }

    /**
     * 获取指定用户的文章（需权限校验）
     * GET http://localhost:8080/api/essay/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public List<EssayDO> getEssaysByUser(@PathVariable Long userId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (currentUsername == null || "anonymousUser".equals(currentUsername)) {
            throw new RuntimeException("用户未登录，无法访问");
        }

        UserDO currentUser = userDAO.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前登录用户不存在"));

        return essayService.getEssaysByUserId(userId);
    }

    /**
     * 根据ID获取文章
     * GET http://localhost:8080/api/essay/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEssayById(@PathVariable String id) {
        EssayDO essay = essayService.getEssayById(id);
        if (essay == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<UserEssayDO> authorAssoc = userEssayService.findAuthorByEssayId(id);
        Long authorId = authorAssoc.map(assoc -> assoc.getUser().getId()).orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("id", essay.getId());
        result.put("title", essay.getTitle());
        result.put("content", essay.getContent());
        result.put("summary", essay.getSummary());
        result.put("coverUrl", essay.getCoverUrl());
        result.put("authorId", authorId);

        result.put("views", essay.getViews());
        result.put("likes", essay.getLikes());
        result.put("createdAt", essay.getCreatedAt());
        result.put("updatedAt", essay.getUpdatedAt());

        return ResponseEntity.ok(result);
    }

    /**
     * 创建新文章
     * POST http://localhost:8080/api/essay
     */
    @PostMapping
    public EssayDO createEssay(@RequestBody EssayDO essayDO) {
        return essayService.createEssay(essayDO);
    }

    /**
     * 更新文章
     * PUT http://localhost:8080/api/essay/{id}
     */
    @PutMapping("/{id}")
    public EssayDO updateEssay(@PathVariable String id, @RequestBody EssayDO essayDO) {
        return essayService.updateEssay(id, essayDO);
    }

    /**
     * 删除文章
     * DELETE http://localhost:8080/api/essay/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteEssay(@PathVariable String id) {
        System.out.println("🎯 DELETE /api/essay/" + id);

        Map<String, Object> response = new HashMap<>();

        try {
            essayService.deleteEssay(id);

            response.put("success", true);
            response.put("message", "文章删除成功");
            response.put("id", id);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (Exception e) {
            System.err.println("❌ 删除失败: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("id", id);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }


    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, Object>> addView(@PathVariable String id) {
        Integer views = essayService.incrementViews(id);
        if (views == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("views", views);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || "anonymousUser".equals(username)) {
            return ResponseEntity.status(401).body("未登录");
        }
        UserDO currentUser = userDAO.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Map<String, Object> result = essayService.toggleLike(id, currentUser.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/like-status")
    public ResponseEntity<?> getLikeStatus(@PathVariable String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = null;
        if (username != null && !"anonymousUser".equals(username)) {
            UserDO user = userDAO.findByUsername(username).orElse(null);
            if (user != null) userId = user.getId();
        }
        Map<String, Object> result = essayService.getLikeStatus(id, userId);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }
}