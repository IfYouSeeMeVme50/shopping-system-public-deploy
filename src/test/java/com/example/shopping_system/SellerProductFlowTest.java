package com.example.shopping_system;

import com.example.shopping_system.Service类.ProductService;
import com.example.shopping_system.Service类.SellerService;
import com.example.shopping_system.Repository接口.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SellerProductFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long primaryCategoryId;
    private Long secondaryCategoryId;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        sellerService.initializeSeller();
        primaryCategoryId = productService.createPrimaryCategory("数码", "电子产品").getId();
        secondaryCategoryId = productService.createSecondaryCategory(primaryCategoryId, "电脑", "电子设备").getId();
    }

    @Test
    void sellerCanPublishProductAndBuyerCanView() throws Exception {
        MockMultipartFile emptyImage = new MockMultipartFile(
                "images",
                "",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[0]
        );

        mockMvc.perform(
                        multipart("/api/seller/product")
                                .file(emptyImage)
                                .param("name", "测试电脑")
                                .param("description", "高性能电脑")
                                .param("price", "4999.00")
                                .param("stock", "5")
                                .param("primaryCategory", String.valueOf(primaryCategoryId))
                                .param("secondaryCategory", String.valueOf(secondaryCategoryId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.id").exists());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("测试电脑"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}
