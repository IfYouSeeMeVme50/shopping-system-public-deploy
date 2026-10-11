package com.example.shopping_system.配置类;

import com.example.shopping_system.实体类.Category;
import com.example.shopping_system.Repository接口.CategoryRepository;
import com.example.shopping_system.Service类.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private CategoryRepository categoryRepository;
    
    // 配置属性，控制是否初始化数据，默认为false
    @Value("${app.initialize-data:false}")
    private boolean initializeData;

    @Override
    public void run(String... args) throws Exception {
        // 只有当initializeData为true时才执行初始化
        if (initializeData) {
            // 初始化默认卖家账号
            sellerService.initializeSeller();
            
            // 初始化默认分类
            initializeCategories();
        }
    }

    private void initializeCategories() {
        // 检查是否已有分类，如果没有则创建默认分类
        if (categoryRepository.count() == 0) {
            // 创建一级分类
            Category electronics = new Category();
            electronics.setId(1L);
            electronics.setName("电子产品");
            electronics.setDescription("手机、电脑、平板等电子产品");
            electronics.setLevel(1);
            categoryRepository.save(electronics);

            Category clothing = new Category();
            clothing.setId(2L);
            clothing.setName("服装服饰");
            clothing.setDescription("衣服、鞋子、配饰等");
            clothing.setLevel(1);
            categoryRepository.save(clothing);

            Category books = new Category();
            books.setId(3L);
            books.setName("图书文具");
            books.setDescription("书籍、文具、办公用品");
            books.setLevel(1);
            categoryRepository.save(books);

            Category home = new Category();
            home.setId(4L);
            home.setName("家居生活");
            home.setDescription("家具、家电、生活用品");
            home.setLevel(1);
            categoryRepository.save(home);

            Category sports = new Category();
            sports.setId(5L);
            sports.setName("运动户外");
            sports.setDescription("运动器材、户外装备");
            sports.setLevel(1);
            categoryRepository.save(sports);


            // 创建二级分类 - 电子产品
            createSecondaryCategory(101L, "手机", "智能手机、功能手机", electronics);
            createSecondaryCategory(102L, "电脑", "笔记本电脑、台式机", electronics);
            createSecondaryCategory(103L, "平板", "平板电脑、电子书", electronics);
            createSecondaryCategory(104L, "配件", "耳机、充电器、保护套等", electronics);

            // 创建二级分类 - 服装服饰
            createSecondaryCategory(201L, "男装", "男士服装、鞋子、配饰", clothing);
            createSecondaryCategory(202L, "女装", "女士服装、鞋子、配饰", clothing);
            createSecondaryCategory(203L, "童装", "儿童服装、鞋子、配饰", clothing);
            createSecondaryCategory(204L, "配饰", "帽子、围巾、手套等", clothing);

            // 创建二级分类 - 图书文具
            createSecondaryCategory(301L, "图书", "文学、科学、技术等各类书籍", books);
            createSecondaryCategory(302L, "文具", "笔、笔记本、文件夹等", books);
            createSecondaryCategory(303L, "办公用品", "办公设备、耗材等", books);

            // 创建二级分类 - 家居生活
            createSecondaryCategory(401L, "家具", "桌椅、沙发、床等", home);
            createSecondaryCategory(402L, "家电", "冰箱、洗衣机、电视等", home);
            createSecondaryCategory(403L, "厨具", "锅碗瓢盆、厨房电器等", home);
            createSecondaryCategory(404L, "日用品", "清洁用品、洗护用品等", home);

            // 创建二级分类 - 运动户外
            createSecondaryCategory(501L, "健身器材", "跑步机、哑铃、健身器材等", sports);
            createSecondaryCategory(502L, "户外装备", "帐篷、背包、登山用品等", sports);
            createSecondaryCategory(503L, "运动服饰", "运动服装、鞋子、配件", sports);



            System.out.println("默认分类初始化完成，包括一级分类和二级分类");
        }
    }
    
    /**
     * 创建二级分类
     * @param id 分类ID
     * @param name 分类名称
     * @param description 分类描述
     * @param parent 父级分类（一级分类）
     */
    private void createSecondaryCategory(Long id, String name, String description, Category parent) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setLevel(2);
        category.setParent(parent);
        categoryRepository.save(category);
    }
}
