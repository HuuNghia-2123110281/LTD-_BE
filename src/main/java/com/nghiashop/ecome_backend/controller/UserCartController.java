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
import com.nghiashop.ecome_backend.dto.Request.AddToCartRequest;
import com.nghiashop.ecome_backend.dto.Request.UpdateCartRequest;
import com.nghiashop.ecome_backend.dto.Response.CartResponse;
import com.nghiashop.ecome_backend.dto.CartItemDTO;
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
            System.out.println("=== GET CART REQUEST ===");
            String email = authentication.getName();
            System.out.println("User email: " + email);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
            
            System.out.println("User ID: " + user.getId());

            Cart cart = cartService.getOrCreateCartByUserId(user.getId());
            System.out.println("Cart ID: " + cart.getId());
            
            List<CartItem> items = cartItemService.findByCartId(cart.getId());
            System.out.println("Cart items count: " + items.size());

            // Log chi tiết từng item
            for (CartItem item : items) {
                System.out.println("  Item ID: " + item.getId() + 
                                 ", Product: " + item.getProduct().getName() + 
                                 ", Quantity: " + item.getQuantity() +
                                 ", Price: " + item.getPrice());
            }

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

            CartResponse response = new CartResponse(
                    cart.getId(),
                    itemDTOs,
                    totalItems,
                    totalPrice);

            System.out.println("Response - Total Items: " + totalItems + ", Total Price: " + totalPrice);
            System.out.println("======================");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR in getCart: " + e.getMessage());
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
            System.out.println("=== ADD TO CART REQUEST ===");
            System.out.println("Product ID: " + request.getProductId());
            System.out.println("Quantity: " + request.getQuantity());
            
            String email = authentication.getName();
            System.out.println("User email: " + email);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
            
            System.out.println("User ID: " + user.getId());

            Cart cart = cartService.getOrCreateCartByUserId(user.getId());
            System.out.println("Cart ID: " + cart.getId());
            
            Product product = productService.getById(request.getProductId());
            System.out.println("Product found: " + product.getName() + ", Stock: " + product.getStock());

            if (product.getStock() < request.getQuantity()) {
                throw new RuntimeException("Không đủ hàng trong kho");
            }

            CartItem existingItem = cartItemService.findByCartIdAndProductId(
                    cart.getId(), 
                    product.getId());

            if (existingItem != null) {
                System.out.println("Existing item found, updating quantity from " + 
                                 existingItem.getQuantity() + " to " + 
                                 (existingItem.getQuantity() + request.getQuantity()));
                existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                CartItem updated = cartItemService.update(existingItem.getId(), existingItem);
                System.out.println("Updated item ID: " + updated.getId() + ", New quantity: " + updated.getQuantity());
            } else {
                System.out.println("Creating new cart item");
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(request.getQuantity());
                newItem.setPrice(product.getPrice());
                CartItem created = cartItemService.create(newItem);
                System.out.println("Created item ID: " + created.getId());
            }

            // Verify lại số lượng items
            List<CartItem> allItems = cartItemService.findByCartId(cart.getId());
            System.out.println("Total items in cart after add: " + allItems.size());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã thêm vào giỏ hàng");
            System.out.println("=========================");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR in addToCart: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("=== UPDATE CART ITEM ===");
            System.out.println("Cart Item ID: " + cartItemId);
            System.out.println("New Quantity: " + request.getQuantity());
            
            CartItem cartItem = cartItemService.getById(cartItemId);

            if (request.getQuantity() < 1) {
                throw new RuntimeException("Số lượng phải lớn hơn 0");
            }

            if (cartItem.getProduct().getStock() < request.getQuantity()) {
                throw new RuntimeException("Không đủ hàng trong kho");
            }

            cartItem.setQuantity(request.getQuantity());
            cartItemService.update(cartItemId, cartItem);
            System.out.println("Updated successfully");
            System.out.println("======================");

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã cập nhật giỏ hàng");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR in updateCartItem: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    // DELETE /api/cart/remove/{cartItemId} - Xóa sản phẩm
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Map<String, String>> removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {
        try {
            System.out.println("=== REMOVE FROM CART ===");
            System.out.println("Cart Item ID: " + cartItemId);
            
            cartItemService.delete(cartItemId);
            System.out.println("Deleted successfully");
            System.out.println("=======================");

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa sản phẩm");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR in removeFromCart: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa: " + e.getMessage());
        }
    }

    // DELETE /api/cart/clear - Xóa toàn bộ giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart(Authentication authentication) {
        try {
            System.out.println("=== CLEAR CART ===");
            String email = authentication.getName();
            System.out.println("User email: " + email);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Cart cart = cartService.findByUserId(user.getId());
            if (cart != null) {
                cartItemService.deleteByCartId(cart.getId());
                System.out.println("Cart cleared successfully");
            }
            System.out.println("==================");

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa toàn bộ giỏ hàng");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR in clearCart: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa giỏ hàng: " + e.getMessage());
        }
    }
}