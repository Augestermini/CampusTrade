-- 为现有校园示例商品补充本地静态商品图片。
-- 图片文件位于 src/main/resources/static/product-images。

UPDATE sh_idle_item
SET picture_list = CONCAT(
    '["http://localhost:8080/product-images/product-',
    id,
    '.jpg"]'
)
WHERE id BETWEEN 100 AND 118;
