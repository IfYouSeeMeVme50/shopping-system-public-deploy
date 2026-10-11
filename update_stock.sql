-- 更新现有商品的库存数量
UPDATE products SET stock = 1 WHERE stock = 0;

-- 验证更新结果
SELECT id, name, stock FROM products;
