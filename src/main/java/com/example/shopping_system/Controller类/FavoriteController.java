package com.example.shopping_system.Controller类;

import com.example.shopping_system.Service类.FavoriteService;
import com.example.shopping_system.实体类.Favorite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 添加商品到收藏夹
     */
    @PostMapping
    public ResponseEntity<?> addToFavorite(@RequestParam String phone, @RequestParam Long productId) {
        try {
            Favorite favorite = favoriteService.addToFavorite(phone, productId);
            return ResponseEntity.ok(favorite);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 从收藏夹移除商品
     */
    @DeleteMapping
    public ResponseEntity<?> removeFromFavorite(@RequestParam String phone, @RequestParam Long productId) {
        try {
            favoriteService.removeFromFavorite(phone, productId);
            return ResponseEntity.ok("商品已从收藏夹移除");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 获取客户的收藏列表
     */
    @GetMapping
    public ResponseEntity<List<Favorite>> getFavoriteList(@RequestParam String phone) {
        List<Favorite> favorites = favoriteService.getFavoriteList(phone);
        return ResponseEntity.ok(favorites);
    }

    /**
     * 检查商品是否在收藏夹中
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFavorite(@RequestParam String phone, @RequestParam Long productId) {
        boolean isFavorited = favoriteService.isFavorite(phone, productId);
        return ResponseEntity.ok(isFavorited);
    }
}