package com.erp.maisPraTi.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tb_delivery_items")
public class DeliveryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne
    @JoinColumn(name = "sale_item_id", nullable = false)
    private SaleItem saleItem;

    private int quantityDelivered;

}

