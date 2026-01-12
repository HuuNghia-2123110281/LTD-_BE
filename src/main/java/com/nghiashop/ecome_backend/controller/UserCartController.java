package com.nghiashop.ecome_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nghiashop.ecome_backend.dto.CartItemDTO;
import com.nghiashop.ecome_backend.dto.Request.AddToCartRequest;
import com.nghiashop.ecome_backend.dto.Request.UpdateCartRequest;
import com.nghiashop.ecome_backend.dto.Response.CartResponse;

import com.nghiashop.ecome_backend.entity.Cart;
import com.nghiashop.ecome_backend.entity.CartItem;
import com.nghiashop.ecome_backend.entity.Product;
import com.nghiashop.ecome_backend.entity.User;
import com.nghiashop.ecome_backend.repository.UserRepository;
import com.nghiashop.ecome_backend.service.CartItemService;
import com.nghiashop.ecome_backend.service.CartService;
import com.nghiashop.ecome_backend.service.ProductService;

@RestController
@RequestMapping("/api/cart")
public class UserCartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    // GET /api/cart - Lấy giỏ hàng của user hiện tại
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Cart cart = cartService.getOrCreateCartByUserId(user.getId());
            List<CartItem> items = cartItemService.findByCartId(cart.getId());

            List<CartItemDTO> itemDTOs = items.stream()
                    .map(item -> new CartItemDTO(
                            item.getId(),
                            item.getProduct(),
                            item.getQuantity(),
                            item.getPrice()))
                    .collect(Collectors.toList());

            double totalPrice = items.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();

            int totalItems = items.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();

            CartResponse response = new CartResponse();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy giỏ hàng: " + e.getMessage());
        }
    }

    // POST /api/cart/add - Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addToCart(
            @RequestBody AddToCartRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Cart cart = cartService.getOrCreateCartByUserId(user.getId());
            Product product = productService.getById(request.getProductId());

            if (product.getStock() < request.getQuantity()) {
                throw new RuntimeException("Không đủ hàng trong kho");
            }

            CartItem existingItem = cartItemService.findByCartIdAndProductId(
                    cart.getId(), 
                    product.getId());

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                cartItemService.update(existingItem.getId(), existingItem);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(request.getQuantity());
                newItem.setPrice(product.getPrice());
                cartItemService.create(newItem);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã thêm vào giỏ hàng");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi thêm vào giỏ: " + e.getMessage());
        }
    }

    // PUT /api/cart/update/{cartItemId} - Cập nhật số lượng
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<Map<String, String>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartRequest request,
            Authentication authentication) {
        try {
            CartItem cartItem = cartItemService.getById(cartItemId);

            if (request.getQuantity() < 1) {
                throw new RuntimeException("Số lượng phải lớn hơn 0");
            }

            if (cartItem.getProduct().getStock() < request.getQuantity()) {
                throw new RuntimeException("Không đủ hàng trong kho");
            }

            cartItem.setQuantity(request.getQuantity());
            cartItemService.update(cartItemId, cartItem);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã cập nhật giỏ hàng");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    // DELETE /api/cart/remove/{cartItemId} - Xóa sản phẩm
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Map<String, String>> removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {
        try {
            cartItemService.delete(cartItemId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa sản phẩm");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa: " + e.getMessage());
        }
    }

    // DELETE /api/cart/clear - Xóa toàn bộ giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Cart cart = cartService.findByUserId(user.getId());
            if (cart != null) {
                cartItemService.deleteByCartId(cart.getId());
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa toàn bộ giỏ hàng");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa giỏ hàng: " + e.getMessage());
        }
    }
}