package com.eric.store.common.bootstrap;

import com.eric.store.brands.entity.Brand;
import com.eric.store.categories.entity.Category;
import com.eric.store.categories.repository.CategoryRepository;
import com.eric.store.files.config.S3Props;
import com.eric.store.files.entity.FileEntity;
import com.eric.store.files.repository.FileRepository;
import com.eric.store.files.service.FileStorageService;
import com.eric.store.products.entity.*;
import com.eric.store.brands.repository.BrandRepository;
import com.eric.store.products.repository.ProductRepository;
import com.eric.store.ratings.entity.Rating;
import com.eric.store.ratings.repository.RatingRepository;
import com.eric.store.user.entity.*;
import com.eric.store.user.repository.RoleRepository;
import com.eric.store.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@Profile({"dev", "docker"})
@RequiredArgsConstructor
@Order(2) // run after RoleSeeder
public class DataSeeder implements CommandLineRunner {

    private final BrandRepository brandRepo;
    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final FileRepository fileRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final RatingRepository ratingRepo;
    private final S3Client s3;
    private final S3Props s3Props;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("Data already seeded, skipping.");
            return;
        }
        log.info("Seeding development data...");

        var asus = brandRepo.save(new Brand("ASUS"));
        var logitech = brandRepo.save(new Brand("Logitech"));
        var sony = brandRepo.save(new Brand("Sony"));
        var apple = brandRepo.save(new Brand("Apple"));
        var samsung = brandRepo.save(new Brand("Samsung"));
        var alienware = brandRepo.save(new Brand("Alienware"));

        var electronics = new Category("Electronics", false);
        var accessories = new Category("Accessories", false);
        var mice = new Category("Computer mice", true);
        var soundEquipment = new Category("Sound equipment", false);
        var headphones = new Category("Headphones", true);
        var earbuds = new Category("Earbuds", true);
        var computers = new Category("Computers", false);
        var laptops = new Category("Laptops", true);
        var desktops = new Category("Desktops", true);
        var phones = new Category("Phones", true);
        soundEquipment.addChild(headphones);
        soundEquipment.addChild(earbuds);
        accessories.addChild(soundEquipment);
        accessories.addChild(mice);
        computers.addChild(laptops);
        computers.addChild(desktops);
        electronics.addChild(accessories);
        electronics.addChild(computers);
        electronics.addChild(phones);

        categoryRepo.save(electronics);

        // --- Products ---
        var zenbook = createProduct("ASUS Zenbook 14", "A light weight Laptop perfect for on-the-go work",
                new BigDecimal("1299.99"), 5, laptops, asus,
                new BigDecimal("30.0"), new BigDecimal("12.0"), new BigDecimal("11.0"), new BigDecimal("0.35"),
                List.of(seedImage("zenbook.jpg")));

        var proart = createProduct("ASUS ProArt 16", "16 inch ryzen ai 9 chip. RTX 5070 gaming laptop",
                new BigDecimal("3099.99"), 80, laptops, asus,
                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
                List.of(seedImage("proart.png")));

        var macbook = createProduct("Macbook Neo", "Super cheap and powerful laptop, only downside is mac OS",
                new BigDecimal("600"), 0, laptops, apple,
                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
                List.of(seedImage("neo.jpg")));

        var mouse = createProduct("G305 Logitech wireless mouse", "wireless mouse >:(",
                new BigDecimal("30"), 2000, mice, logitech,
                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
                List.of(seedImage("195809-logitech-g305-black-1.avif")));

        var headset = createProduct("Headset 7.1 Logitech G432", "wireless headphones",
                new BigDecimal("100"), 1, headphones, logitech,
                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
                List.of(seedImage("219478-1549404057000-img-1134809-medium.avif")));

        var iphone = createProduct("iPhone 17 Pro Max", "test",
                new BigDecimal("50"), 1, phones, apple,
                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
                List.of(seedImage("Apple-iPhone-17-Pro-cosmic-orange-250909_inline.jpg.large.jpg")));

        var galaxy = createProduct("Galaxy S26 Ultra", "test",
                new BigDecimal("50"), 1, phones, samsung,
                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
                List.of(seedImage("446972-675684-medium.avif")));

        var galaxyBook = createProduct("Samsung Galaxy Book4", "test",
                new BigDecimal("50"), 1, laptops, samsung,
                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
                List.of(seedImage("laptop-samsung-galaxy-book4-pro-16-np960xgk-kg1es-16-intel-evo-core-ultra-7-155h-16-gb-ram-512-gb-s.jpg")));

        var alienwarePC = createProduct("Alienware gaming pc", "test",
                new BigDecimal("50"), 1, desktops, alienware,
                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
                List.of(seedImage("uWWBLWsabXHaERxF3QRntW.jpg")));

        var airpods = createProduct("AirPods", "test",
                new BigDecimal("50"), 1, earbuds, apple,
                new BigDecimal("31.0"), new BigDecimal("12.5"), new BigDecimal("11.5"), new BigDecimal("0.31"),
                List.of(seedImage("apple-airpodspro3-mfhp4zma-dfa4f-hind_large.webp")));

        var eric = seedUser("eric.rand@gmail.com", "Eric Rand", "Eric@123", "USER");
        var testUser = seedUser("user@store.dev", "Test User", "User@123", "USER");
        var jane = seedUser("jane@store.dev", "Jane Doe", "Jane@123", "USER");

        // --- Ratings ---
        seedRating(eric, zenbook, 5, "Best ultrabook I've ever owned, super lightweight");
        seedRating(testUser, zenbook, 4, "Great laptop but battery could be better");
        seedRating(jane, zenbook, 5, "Perfect for travel");

        seedRating(eric, proart, 5, "Incredible for creative work and gaming");
        seedRating(testUser, proart, 4, "Powerful but runs hot under load");

        seedRating(eric, macbook, 3, "Good hardware, not a fan of macOS");
        seedRating(jane, macbook, 4, "Surprisingly affordable for Apple");

        seedRating(testUser, mouse, 5, "Best budget wireless mouse out there");
        seedRating(jane, mouse, 4, "Solid mouse, good battery life");
        seedRating(eric, mouse, 4, "Great value for the price");

        seedRating(eric, headset, 3, "Decent sound but not very comfortable");
        seedRating(testUser, headset, 4, "Good surround sound for the price");

        seedRating(jane, iphone, 5, "Love the camera and design");
        seedRating(eric, iphone, 4, "Great phone, a bit pricey though");

        seedRating(testUser, galaxy, 5, "Samsung nailed it with this one");
        seedRating(jane, galaxy, 4, "Beautiful display, solid performance");

        seedRating(eric, galaxyBook, 3, "Decent laptop, nothing special");

        seedRating(testUser, alienwarePC, 5, "Beast of a machine for gaming");
        seedRating(jane, alienwarePC, 5, "Runs everything at max settings");

        seedRating(eric, airpods, 4, "Great sound quality, comfortable fit");
        seedRating(jane, airpods, 3, "Good but overpriced for what you get");

        log.info("Development data seeded successfully.");
    }

    /**
     * Reads an image from classpath (seed/images/{filename}),
     * uploads it to MinIO, and returns a saved FileEntity.
     */
    private FileEntity seedImage(String filename) {
        try {
            var resource = new ClassPathResource("seed/images/" + filename);
            byte[] bytes = resource.getInputStream().readAllBytes();

            String contentType = guessContentType(filename);

            String key = "public/seed/" + UUID.randomUUID() + "_" + filename;
            String url = s3Props.publicBaseUrl() + "/" + key;

            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Props.bucket())
                            .key(key)
                            .contentType(contentType)
                            .cacheControl("public, max-age=31536000")
                            .build(),
                    RequestBody.fromBytes(bytes)
            );

            String thumbnailUrl = null;
            try {
                thumbnailUrl = fileStorageService.generateAndUploadThumbnailFromBytes(bytes, key);
            } catch (Exception ignored) {
                // avif/webp not supported by ImageIO — thumbnail skipped
            }

            var file = new FileEntity();
            file.setKey(key);
            file.setUrl(url);
            file.setThumbnailUrl(thumbnailUrl);
            file.setContentType(contentType);
            file.setSizeBytes((long) bytes.length);
            return fileRepo.save(file);

        } catch (IOException e) {
            throw new RuntimeException("Failed to seed image: " + filename, e);
        }
    }

    private static final Map<String, String> EXTRA_MIME = Map.of(
            ".avif", "image/avif",
            ".webp", "image/webp"
    );

    private static String guessContentType(String filename) {
        String lower = filename.toLowerCase();
        for (var entry : EXTRA_MIME.entrySet()) {
            if (lower.endsWith(entry.getKey())) return entry.getValue();
        }
        String guess = URLConnection.guessContentTypeFromName(filename);
        return guess != null ? guess : "application/octet-stream";
    }

    private Product createProduct(String name, String description, BigDecimal price,
                                  int stock, Category category, Brand brand,
                                  BigDecimal width, BigDecimal height, BigDecimal depth,
                                  BigDecimal weight, List<FileEntity> imageFiles) {
        var product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCurrency(CurrencyProvider.EUR);
        product.setStock(stock);
        product.setBrand(brand);
        product.setWidth(width);
        product.setHeight(height);
        product.setDepth(depth);
        product.setWeight(weight);
        product.setCategory(category);

        for (int i = 0; i < imageFiles.size(); i++) {
            product.addImage(new ProductImage(imageFiles.get(i), i));
        }

        return productRepo.save(product);
    }


    private User seedUser(String email, String name, String password, String... roleNames) {
        if (userRepo.existsByEmail(email)) return userRepo.findByEmail(email).orElseThrow();

        var user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setProvider(AuthProvider.LOCAL);
        user.setEmailVerified(true);

        for (String roleName : roleNames) {
            roleRepo.findByName(roleName).ifPresent(user.getRoles()::add);
        }

        var settings = new UserSettings();
        settings.setUser(user);
        user.setSettings(settings);

        return userRepo.save(user);
    }

    private void seedRating(User user, Product product, int score, String comment) {
        var rating = new Rating();
        rating.setUser(user);
        rating.setProduct(product);
        rating.setScore(score);
        rating.setComment(comment);
        ratingRepo.save(rating);

        ratingRepo.flush();
        product.setAverageRating(ratingRepo.averageScoreByProductId(product.getId()));
        product.setTotalRatings(ratingRepo.countByProductId(product.getId()));
        productRepo.save(product);
    }
}
