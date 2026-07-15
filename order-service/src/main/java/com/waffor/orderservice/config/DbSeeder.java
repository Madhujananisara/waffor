package com.waffor.orderservice.config;

import com.waffor.orderservice.entity.FoodItem;
import com.waffor.orderservice.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DbSeeder implements CommandLineRunner {

    private final FoodItemRepository foodItemRepository;
    private final com.waffor.orderservice.repository.UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Seed Admin User
        if (userRepository.count() == 0) {
            log.info("🌱 Seeding admin account...");
            userRepository.save(com.waffor.orderservice.entity.User.builder()
                .email("admin@waffor.com")
                .password("admin")
                .name("Waffor Admin")
                .role("ADMIN")
                .build());
        }

        if (foodItemRepository.count() == 0) {
            log.info("🌱 Seeding database with initial food menu items...");

            List<FoodItem> items = List.of(
                FoodItem.builder()
                    .name("Chicken Pizza")
                    .description("Cheese loaded pizza with grilled chicken, onions, and bell peppers")
                    .price(new BigDecimal("299.00"))
                    .imageUrl("pizza")
                    .isVeg(false)
                    .rating(new BigDecimal("4.5"))
                    .build(),
                FoodItem.builder()
                    .name("Margherita Pizza")
                    .description("Classic tomato sauce, fresh mozzarella cheese and fresh basil leaves")
                    .price(new BigDecimal("199.00"))
                    .imageUrl("pizza")
                    .isVeg(true)
                    .rating(new BigDecimal("4.2"))
                    .build(),
                FoodItem.builder()
                    .name("Paneer Tikka Burger")
                    .description("Crispy paneer patty with spicy tikka sauce, lettuce, and onions")
                    .price(new BigDecimal("149.00"))
                    .imageUrl("burger")
                    .isVeg(true)
                    .rating(new BigDecimal("4.3"))
                    .build(),
                FoodItem.builder()
                    .name("Crispy Chicken Burger")
                    .description("Succulent crispy chicken breast fillet with fresh lettuce and mayo")
                    .price(new BigDecimal("169.00"))
                    .imageUrl("burger")
                    .isVeg(false)
                    .rating(new BigDecimal("4.6"))
                    .build(),
                FoodItem.builder()
                    .name("White Sauce Pasta")
                    .description("Penne pasta tossed in rich cheesy white cream sauce with mushrooms")
                    .price(new BigDecimal("229.00"))
                    .imageUrl("pasta")
                    .isVeg(true)
                    .rating(new BigDecimal("4.4"))
                    .build(),
                FoodItem.builder()
                    .name("Garlic Bread Sticks")
                    .description("Warm and freshly baked garlic bread sticks served with cheese dip")
                    .price(new BigDecimal("99.00"))
                    .imageUrl("bread")
                    .isVeg(true)
                    .rating(new BigDecimal("4.0"))
                    .build(),
                FoodItem.builder()
                    .name("Spicy Chicken Wings")
                    .description("6 pieces of chicken wings glazed with signature peri-peri hot sauce")
                    .price(new BigDecimal("249.00"))
                    .imageUrl("wings")
                    .isVeg(false)
                    .rating(new BigDecimal("4.7"))
                    .build(),
                FoodItem.builder()
                    .name("Fudge Chocolate Brownie")
                    .description("Warm chocolate fudge brownie loaded with melting choco chips")
                    .price(new BigDecimal("119.00"))
                    .imageUrl("brownie")
                    .isVeg(true)
                    .rating(new BigDecimal("4.8"))
                    .build()
            );

            foodItemRepository.saveAll(items);
            log.info("✅ Seeding complete. Added {} food items to catalog.", items.size());
        } else {
            log.info("🍔 Food menu catalog already seeded ({} items found). Skipping.", foodItemRepository.count());
        }
    }
}
