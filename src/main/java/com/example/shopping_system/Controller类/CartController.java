package com.example.shopping_system.Controller类;

import com.example.shopping_system.Service类.CartService;
import com.example.shopping_system.实体类.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    /**
     * 获取购物车列表
     */
    @GetMapping("/cart")
    public ResponseEntity<?> getCart(@RequestParam String customerPhone) {
        try {
            List<Cart> cartItems = cartService.getCartList(customerPhone);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "获取购物车列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 添加商品到购物车
     */
    @PostMapping("/cart")
    public ResponseEntity<?> addToCart(@RequestParam String customerPhone, 
                                      @RequestParam Long productId, 
                                      @RequestParam int quantity) {
        try {
            Cart cart = cartService.addToCart(customerPhone, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "添加商品到购物车失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新购物车商品数量
     */
    @PutMapping("/cart")
    public ResponseEntity<?> updateCartQuantity(@RequestParam String customerPhone, 
                                               @RequestParam Long productId, 
                                               @RequestParam int quantity) {
        try {
            Cart cart = cartService.updateCartQuantity(customerPhone, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "更新购物车商品数量失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 从购物车移除商品
     */
    @DeleteMapping("/cart")
    public ResponseEntity<?> removeFromCart(@RequestParam String customerPhone, 
                                           @RequestParam Long productId) {
        try {
            cartService.removeFromCart(customerPhone, productId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "从购物车移除商品成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "从购物车移除商品失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping("/cart/clear")
    public ResponseEntity<?> clearCart(@RequestParam String customerPhone) {
        try {
            cartService.clearCart(customerPhone);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "清空购物车成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "清空购物车失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取购物车商品总数
     */
    @GetMapping("/cart/count")
    public ResponseEntity<?> getCartCount(@RequestParam String customerPhone) {
        try {
            Long count = cartService.getCartItemCount(customerPhone);
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "获取购物车商品总数失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}