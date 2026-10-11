package com.example.shopping_system.配置类;

import com.example.shopping_system.Repository接口.CustomerRepository;
import com.example.shopping_system.Repository接口.ProductRepository;
import com.example.shopping_system.Repository接口.PurchaseIntentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleanupRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanupRunner.class);

    private final PurchaseIntentRepository purchaseIntentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final boolean autoClean;

    public DatabaseCleanupRunner(PurchaseIntentRepository purchaseIntentRepository,
                                 ProductRepository productRepository,
                                 CustomerRepository customerRepository,
                                 @Value("${app.auto-clean:false}") boolean autoClean) {
        this.purchaseIntentRepository = purchaseIntentRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.autoClean = autoClean;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!autoClean) {
            return;
        }

        logger.info("Starting cleanup of purchase intents, products and customers...");
        try {
            // deleteAllInBatch 避免因缺失关联记录而在加载实体时抛出 EntityNotFoundException
            purchaseIntentRepository.deleteAllInBatch();
            productRepository.deleteAllInBatch();
            customerRepository.deleteAllInBatch();
            logger.info("Cleanup completed. All buyer and product data have been removed.");
        } catch (Exception ex) {
            logger.warn("Cleanup skipped due to error: {}", ex.getMessage());
        }
    }
}
