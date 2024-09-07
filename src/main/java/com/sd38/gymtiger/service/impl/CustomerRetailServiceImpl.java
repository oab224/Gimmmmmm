package com.sd38.gymtiger.service.impl;

import com.sd38.gymtiger.model.CustomerRetail;
import com.sd38.gymtiger.repository.CustomerRetailRepository;
import com.sd38.gymtiger.service.CustomerRetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerRetailServiceImpl implements CustomerRetailService {
    @Autowired
    private CustomerRetailRepository customerRetailRepository;
    @Override
    public List<CustomerRetail> GetAll() {
        return customerRetailRepository.findAll();
    }

    @Override
    public CustomerRetail Add(CustomerRetail customerRetail) {
        try{
            return customerRetailRepository.save(customerRetail);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public CustomerRetail getCustomerRetailById(Integer id) {
        return customerRetailRepository.findById(id).orElse(null);
    }
}
