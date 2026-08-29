package org.example.backend.inventory;

import jakarta.persistence.*;
import org.example.backend.product.Product;
import org.example.backend.purchase.Purchase;
import org.example.backend.purchase.PurchaseItem;
import org.example.backend.shop.Shop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_batch")
public class InventoryBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(
            name = "selling_discount_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal sellingDiscountPercentage;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_item_id")
    private PurchaseItem purchaseItem;

    public Long getId() {
        return id;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public void setSellingDiscountPercentage(
            BigDecimal sellingDiscountPercentage
    ) {
        this.sellingDiscountPercentage = sellingDiscountPercentage;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public void setPurchaseItem(PurchaseItem purchaseItem) {
        this.purchaseItem = purchaseItem;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public BigDecimal getMrp() {
        return mrp;
    }
    public BigDecimal getSellingDiscountPercentage() {
        return sellingDiscountPercentage;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public PurchaseItem getPurchaseItem() {
        return purchaseItem;
    }
    public Shop getShop() {
        return shop;
    }
    public Purchase getPurchase() {
        return purchase;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Product getProduct() {
        return product;
    }
    public BigDecimal getSellingDiscount() {
        return sellingDiscountPercentage;
    }
}