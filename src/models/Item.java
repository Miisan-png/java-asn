package models;

public class Item {
    private String itemCode;
    private String itemName;
    private String supplierId;
    private int stockQuantity;
    private double pricePerUnit;

    public Item() {
    }

    public Item(String itemCode, String itemName, String supplierId) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.supplierId = supplierId;
        this.stockQuantity = 0;
        this.pricePerUnit = 0.0;
    }

    public Item(String itemCode, String itemName, String supplierId, int stockQuantity, double pricePerUnit) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.supplierId = supplierId;
        this.stockQuantity = stockQuantity;
        this.pricePerUnit = pricePerUnit;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public boolean validateItemData() {
        return itemCode != null && !itemCode.trim().isEmpty() &&
               itemName != null && !itemName.trim().isEmpty() &&
               supplierId != null && !supplierId.trim().isEmpty() &&
               stockQuantity >= 0 &&
               pricePerUnit >= 0.0;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", supplierId='" + supplierId + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", pricePerUnit=" + pricePerUnit +
                '}';
    }
}


