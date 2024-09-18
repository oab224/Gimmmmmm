package com.sd38.gymtiger.repository;

import com.sd38.gymtiger.model.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail,Integer> {
    @Query("SELECT c FROM CartDetail c WHERE c.cart.id =:idCart AND c.productDetail.id =:idProductDetail")
    CartDetail getNumberOfCart(@Param("idCart")  Integer idCart, @Param("idProductDetail") Integer idProductDetail);
}
