package com.sd38.gymtiger.service;

import com.sd38.gymtiger.model.Brand;
import com.sd38.gymtiger.model.CustomerRetail;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
// Interface of service customer retail
public interface CustomerRetailService {
    List<CustomerRetail> GetAll();
    CustomerRetail Add(CustomerRetail customerRetail);
}
