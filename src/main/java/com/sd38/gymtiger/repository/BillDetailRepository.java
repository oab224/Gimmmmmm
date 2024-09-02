package com.sd38.gymtiger.repository;

import com.sd38.gymtiger.model.BillDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillDetailRepository extends JpaRepository<BillDetail, Integer> {
    List<BillDetail> findAllByStatus(Integer status);

    @Query("select b from BillDetail b where b.bill.id=?1 and b.status=1")
    List<BillDetail> findAllByBill_Id(Integer id);

    Page<BillDetail> findAllByStatus(Pageable pageable, Integer status);

    BillDetail findByBill_IdAndProductDetail_Id(Integer billId, Integer productDetailId);
}
