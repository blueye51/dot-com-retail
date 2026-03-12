//package com.eric.store.common.bootstrap;
//
//import com.eric.store.categories.entity.Category;
//import com.eric.store.categories.repository.CategoryRepository;
//import com.eric.store.files.entity.FileEntity;
//import com.eric.store.files.repository.FileRepository;
//import com.eric.store.products.entity.*;
//import com.eric.store.brands.repository.BrandRepository;
//import com.eric.store.products.repository.ProductRepository;
//import com.eric.store.user.entity.*;
//import com.eric.store.user.repository.RoleRepository;
//import com.eric.store.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Slf4j
//@Component
//@Profile({"dev", "docker"})
//@RequiredArgsConstructor
//@Order(2) // run after RoleSeeder
//public class DataSeeder implements CommandLineRunner {
//
//    private final BrandRepository brandRepo;
//    private final CategoryRepository categoryRepo;
//    private final ProductRepository productRepo;
//    private final FileRepository fileRepo;
//    private final UserRepository userRepo;
//    private final RoleRepository roleRepo;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        if (productRepo.count() > 0) {
//            log.info("Data already seeded, skipping.");
//            return;
//        }
//        log.info("Seeding development data...");
//
//        // --- Brands ---
//        var nike      = brandRepo.save(new Brand("Nike"));
//        var adidas    = brandRepo.save(new Brand("Adidas"));
//        var sony      = brandRepo.save(new Brand("Sony"));
//        var apple     = brandRepo.save(new Brand("Apple"));
//        var samsung   = brandRepo.save(new Brand("Samsung"));
//        var lego      = brandRepo.save(new Brand("LEGO"));
//        var ikea      = brandRepo.save(new Brand("IKEA"));
//        var northFace = brandRepo.save(new Brand("The North Face"));
//
//        // --- Categories (hierarchy) ---
//        // Clothing (parent) -> Shoes, T-Shirts
//        var clothing = new Category("Clothing", false);
//        var shoes    = new Category("Shoes", true);
//        var tshirts  = new Category("T-Shirts", true);
//        clothing.addChild(shoes);
//        clothing.addChild(tshirts);
//        categoryRepo.save(clothing);
//
//        // Electronics (parent) -> Phones, Headphones, Laptops
//        var electronics = new Category("Electronics", false);
//        var phones      = new Category("Phones", true);
//        var headphones  = new Category("Headphones", true);
//        var laptops     = new Category("Laptops", true);
//        electronics.addChild(phones);
//        electronics.addChild(headphones);
//        electronics.addChild(laptops);
//        categoryRepo.save(electronics);
//
//        // Home & Garden (parent) -> Furniture, Decor
//        var homeGarden = new Category("Home & Garden", false);
//        var furniture   = new Category("Furniture", true);
//        var decor       = new Category("Decor", true);
//        homeGarden.addChild(furniture);
//        homeGarden.addChild(decor);
//        categoryRepo.save(homeGarden);
//
//        // Toys (leaf)
//        var toys = categoryRepo.save(new Category("Toys", true));
//
//        // Outdoors (parent) -> Jackets, Backpacks
//        var outdoors  = new Category("Outdoors", false);
//        var jackets   = new Category("Jackets", true);
//        var backpacks = new Category("Backpacks", true);
//        outdoors.addChild(jackets);
//        outdoors.addChild(backpacks);
//        categoryRepo.save(outdoors);
//
//        // --- Placeholder file entities for product images ---
//        var files = createPlaceholderFiles(20);
//
//        // --- Products ---
//        createProduct("Air Max 90", "Classic Nike running shoe with visible Air cushioning.",
//                new BigDecimal("129.99"), 150, shoes, nike,
//                new BigDecimal("30.0"), new BigDecimal("12.0"), new BigDecimal("11.0"), new BigDecimal("0.35"),
//                files.subList(0, 2));
//
//        createProduct("Ultraboost 22", "Responsive Adidas running shoe with Boost midsole.",
//                new BigDecimal("189.99"), 80, shoes, adidas,
//                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
//                files.subList(2, 4));
//
//        createProduct("Dri-FIT Training Tee", "Lightweight moisture-wicking t-shirt for workouts.",
//                new BigDecimal("34.99"), 300, tshirts, nike,
//                null, null, null, new BigDecimal("0.15"),
//                files.subList(4, 5));
//
//        createProduct("Trefoil Essentials Tee", "Soft cotton tee with the iconic Adidas trefoil logo.",
//                new BigDecimal("29.99"), 250, tshirts, adidas,
//                null, null, null, new BigDecimal("0.18"),
//                files.subList(5, 6));
//
//        createProduct("iPhone 15 Pro", "Apple smartphone with A17 Pro chip and titanium design.",
//                new BigDecimal("1199.00"), 45, phones, apple,
//                new BigDecimal("7.1"), new BigDecimal("14.7"), new BigDecimal("0.83"), new BigDecimal("0.187"),
//                files.subList(6, 8));
//
//        createProduct("Galaxy S24 Ultra", "Samsung flagship with S Pen and 200MP camera.",
//                new BigDecimal("1299.99"), 35, phones, samsung,
//                new BigDecimal("7.9"), new BigDecimal("16.2"), new BigDecimal("0.86"), new BigDecimal("0.232"),
//                files.subList(8, 10));
//
//        createProduct("WH-1000XM5", "Industry-leading noise cancelling wireless headphones.",
//                new BigDecimal("349.99"), 60, headphones, sony,
//                new BigDecimal("19.0"), new BigDecimal("23.0"), new BigDecimal("6.0"), new BigDecimal("0.25"),
//                files.subList(10, 12));
//
//        createProduct("MacBook Air M3", "Ultra-thin laptop with Apple M3 chip and 18-hour battery.",
//                new BigDecimal("1099.00"), 25, laptops, apple,
//                new BigDecimal("30.4"), new BigDecimal("1.13"), new BigDecimal("21.5"), new BigDecimal("1.24"),
//                files.subList(12, 13));
//
//        createProduct("KALLAX Shelf Unit", "Versatile IKEA storage unit, 4x4 compartments.",
//                new BigDecimal("99.99"), 40, furniture, ikea,
//                new BigDecimal("147.0"), new BigDecimal("147.0"), new BigDecimal("39.0"), new BigDecimal("42.0"),
//                files.subList(13, 14));
//
//        createProduct("SMÄLLEN Vase", "Handmade glass vase with a modern Scandinavian design.",
//                new BigDecimal("24.99"), 120, decor, ikea,
//                new BigDecimal("12.0"), new BigDecimal("22.0"), new BigDecimal("12.0"), new BigDecimal("0.8"),
//                files.subList(14, 15));
//
//        createProduct("LEGO Technic Porsche 911", "1,458-piece building set with working gearbox.",
//                new BigDecimal("149.99"), 55, toys, lego,
//                new BigDecimal("48.0"), new BigDecimal("28.0"), new BigDecimal("9.0"), new BigDecimal("1.6"),
//                files.subList(15, 17));
//
//        createProduct("Nuptse Puffer Jacket", "Iconic The North Face puffer with 700-fill goose down.",
//                new BigDecimal("280.00"), 70, jackets, northFace,
//                null, null, null, new BigDecimal("0.85"),
//                files.subList(17, 18));
//
//        createProduct("Borealis Backpack", "Durable The North Face daypack with laptop compartment.",
//                new BigDecimal("99.00"), 90, backpacks, northFace,
//                new BigDecimal("34.0"), new BigDecimal("50.0"), new BigDecimal("22.0"), new BigDecimal("1.19"),
//                files.subList(18, 20));
//
//        // --- Users ---
//        seedUser("admin@store.dev", "Admin User", "admin123", "ADMIN", "USER");
//        seedUser("user@store.dev", "Test User", "user123", "USER");
//        seedUser("jane@store.dev", "Jane Doe", "jane123", "USER");
//
//        log.info("Development data seeded successfully.");
//    }
//
//    private void createProduct(String name, String description, BigDecimal price,
//                               int stock, Category category, Brand brand,
//                               BigDecimal width, BigDecimal height, BigDecimal depth,
//                               BigDecimal weight, List<FileEntity> imageFiles) {
//        var product = new Product();
//        product.setName(name);
//        product.setDescription(description);
//        product.setPrice(price);
//        product.setCurrency(CurrencyProvider.EUR);
//        product.setStock(stock);
//        product.setBrand(brand);
//        product.setWidth(width);
//        product.setHeight(height);
//        product.setDepth(depth);
//        product.setWeight(weight);
//        category.addProduct(product);
//
//        for (int i = 0; i < imageFiles.size(); i++) {
//            product.addImage(new ProductImage(imageFiles.get(i), i));
//        }
//
//        productRepo.save(product);
//    }
//
//    private List<FileEntity> createPlaceholderFiles(int count) {
//        return java.util.stream.IntStream.range(0, count)
//                .mapToObj(i -> {
//                    var file = new FileEntity();
//                    file.setKey("seed/product-image-" + i + ".jpg");
//                    file.setUrl("https://placehold.co/800x800/png?text=Product+" + i);
//                    file.setContentType("image/jpeg");
//                    file.setSizeBytes(50_000L);
//                    return fileRepo.save(file);
//                })
//                .toList();
//    }
//
//    private void seedUser(String email, String name, String password, String... roleNames) {
//        if (userRepo.existsByEmail(email)) return;
//
//        var user = new User();
//        user.setEmail(email);
//        user.setName(name);
//        user.setPasswordHash(passwordEncoder.encode(password));
//        user.setProvider(AuthProvider.LOCAL);
//        user.setEmailVerified(true);
//
//        for (String roleName : roleNames) {
//            roleRepo.findByName(roleName).ifPresent(user.getRoles()::add);
//        }
//
//        var settings = new UserSettings();
//        settings.setUser(user);
//        user.setSettings(settings);
//
//        userRepo.save(user);
//    }
//}
