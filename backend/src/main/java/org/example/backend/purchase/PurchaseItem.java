package org.example.backend.purchase;

import jakarta.persistence.*;
import org.example.backend.product.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_item")
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(
            name = "dealer_discount_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal dealerDiscountPercentage;

    @Column(
            name = "purchase_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal purchasePrice;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Purchase getPurchase() {
        return purchase;
    }
    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public BigDecimal getMrp() {
        return mrp;
    }
    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }
    public BigDecimal getDealerDiscountPercentage() {
        return dealerDiscountPercentage;
    }
    public void setDealerDiscountPercentage(BigDecimal dealerDiscountPercentage) {
        this.dealerDiscountPercentage = dealerDiscountPercentage;
    }
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
}