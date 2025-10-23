package com.example.InventoryManagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "商品名は必須です")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "数量は必須です")
    @Min(value = 0, message = "数量は0以上で入力してください")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "単価は必須です")
    @Min(value = 1, message = "単価は1以上で入力してください")
    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    public Product() {
    }

    public Product(String name, Integer quantity, Integer unitPrice) {
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // getters / setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Integer unitPrice) {
		this.unitPrice = unitPrice;
	}

    @Transient
    public Integer getTotalPrice() {
    	// NullPointerExceptionを回避するためにnullチェックを強化
        if (quantity == null || unitPrice == null) {
            return 0;
        }
        return quantity * unitPrice;
    }
}