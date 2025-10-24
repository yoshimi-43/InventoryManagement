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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Transient
    public Integer getTotalPrice() {
        // NullPointerExceptionを回避するためにnullチェックを強化
        if (quantity == null || unitPrice == null) {
            return 0;
        }
        return quantity * unitPrice;
    }
}
