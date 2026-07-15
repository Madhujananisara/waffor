package com.waffor.orderservice.controller;

import com.waffor.orderservice.entity.FoodItem;
import com.waffor.orderservice.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
@Slf4j
public class FoodItemController {

    private final FoodItemRepository foodItemRepository;

    @GetMapping
    public ResponseEntity<List<FoodItem>> getMenu() {
        log.info("REST request to fetch all food items");
        List<FoodItem> items = foodItemRepository.findAll();
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<FoodItem> addFoodItem(@RequestBody FoodItem foodItem) {
        log.info("REST request to add new food item: {}", foodItem.getName());
        FoodItem saved = foodItemRepository.save(foodItem);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<FoodItem> updateFoodItemPrice(@PathVariable Long id, @RequestBody Map<String, BigDecimal> payload) {
        BigDecimal price = payload.get("price");
        log.info("REST request to update price of food item: {} to {}", id, price);
        return foodItemRepository.findById(id)
                .map(item -> {
                    item.setPrice(price);
                    FoodItem updated = foodItemRepository.save(item);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
