package com.example.externalapi.domain;

import java.util.List;

public final class Item {

        private final String itemId;
        private final String itemName;
        private final String categoryId;
        private final String brandId;
        private final int unitPrice;
        private final String unit;
        private final int stock;
        private final List<String> tags;

        public Item(String itemId, String itemName, int unitPrice, String unit, int stock) {
                this(itemId, itemName, null, null, unitPrice, unit, stock, List.of());
        }

        public Item(
                        String itemId,
                        String itemName,
                        String categoryId,
                        String brandId,
                        int unitPrice,
                        String unit,
                        int stock,
                        List<String> tags
        ) {
                this.itemId = itemId;
                this.itemName = itemName;
                this.categoryId = categoryId;
                this.brandId = brandId;
                this.unitPrice = unitPrice;
                this.unit = unit;
                this.stock = stock;
                this.tags = List.copyOf(tags);
        }

        public String itemId() {
                return itemId;
        }

        public String itemName() {
                return itemName;
        }

        public String categoryId() {
                return categoryId;
        }

        public String brandId() {
                return brandId;
        }

        public int unitPrice() {
                return unitPrice;
        }

        public String unit() {
                return unit;
        }

        public int stock() {
                return stock;
        }

        public List<String> tags() {
                return tags;
        }
}
