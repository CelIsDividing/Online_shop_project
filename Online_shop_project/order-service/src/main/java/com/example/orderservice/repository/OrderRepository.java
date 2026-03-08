package com.example.orderservice.repository;

import com.example.orderservice.dto.TopExtraProjection;
import com.example.orderservice.dto.TopProductProjection;
import com.example.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = 'DRAFT' ORDER BY o.createdAt DESC")
    List<Order> findDraftsByUserId(@Param("userId") Long userId);

    @Query(value = """
            SELECT oi.product_id AS productId,
                   oi.product_name AS productName,
                   COUNT(oi.id) AS timesSold,
                   SUM(oi.price) AS totalRevenue
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id AND o.status = 'PAID'
            GROUP BY oi.product_id, oi.product_name
            ORDER BY totalRevenue DESC
            """, nativeQuery = true)
    List<TopProductProjection> findTopProducts();

    @Query(value = """
            SELECT oie.extra_id AS extraId,
                   oie.extra_name AS extraName,
                   COUNT(oie.id) AS timesUsed,
                   SUM(oie.price) AS revenue
            FROM order_item_extras oie
            GROUP BY oie.extra_id, oie.extra_name
            ORDER BY timesUsed DESC
            """, nativeQuery = true)
    List<TopExtraProjection> findTopExtras();
}
