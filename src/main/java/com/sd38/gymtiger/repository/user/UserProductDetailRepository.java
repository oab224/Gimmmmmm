package com.sd38.gymtiger.repository.user;

import com.sd38.gymtiger.model.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserProductDetailRepository extends JpaRepository<ProductDetail, Integer> {
    @Query("SELECT p FROM ProductDetail p WHERE p.status = 1 AND p.product.id = :id")
    List<ProductDetail> getAllProductDetail(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE ProductDetail p SET p.quantity = :quantity WHERE p.id = :id")
    void updateNumberProduct(@Param("quantity") Integer quantity, @Param("id") int id);

    @Query(value = "SELECT Id FROM ProductDetail WHERE Status = 1 AND ProductId = :productId AND SizeId = :sizeId AND ColorId = :colorId", nativeQuery = true)
    Integer getProductDetailId(@Param("productId") Integer productId, @Param("sizeId") Integer sizeId, @Param("colorId") Integer colorId);

    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.size.id = :sizeId AND pd.color.id = :colorId")
    ProductDetail getProductDetail(@Param("productId") Integer productId, @Param("sizeId") Integer sizeId, @Param("colorId") Integer colorId);
}
