package com.sd38.gymtiger.repository;

import com.sd38.gymtiger.model.CustomerRetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CustomerRetailRepository extends JpaRepository<CustomerRetail, Integer> {
}
