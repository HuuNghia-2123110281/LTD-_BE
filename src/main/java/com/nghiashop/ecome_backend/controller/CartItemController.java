package com.nghiashop.ecome_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nghiashop.ecome_backend.entity.CartItem;
import com.nghiashop.ecome_backend.service.CartItemService;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;

    @GetMapping
    public List<CartItem> getAll() {
        return cartItemService.getAll();
    }

    @PostMapping
    public CartItem create(@RequestBody CartItem cartItem) {
        return cartItemService.create(cartItem);
    }

    @PutMapping("/{id}")
    public CartItem update(@PathVariable Long id, @RequestBody CartItem cartItem) {
        return cartItemService.update(id, cartItem);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        cartItemService.delete(id);
    }
}
