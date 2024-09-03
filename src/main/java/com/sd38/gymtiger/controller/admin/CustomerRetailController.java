package com.sd38.gymtiger.controller.admin;

import com.sd38.gymtiger.model.CustomerRetail;
import com.sd38.gymtiger.service.CustomerRetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tiger/customer-retail")
public class CustomerRetailController {
    @Autowired
    private CustomerRetailService customerRetailService;
    // Function add customer retail
    @PostMapping("/add/customer-retail")
    public String Create(@ModelAttribute CustomerRetail customerRetail, RedirectAttributes attributes) {
        // Call back create service
        var response = customerRetailService.Add(customerRetail);
        if(response != null && response.getId() != null) {
            attributes.addFlashAttribute("mess", 1);
            return "redirect:/tiger/pos/listKH_tai_quay";
        }else{
            attributes.addFlashAttribute("mess", 0);
            return "redirect:/tiger/pos/listKH_tai_quay";
        }
    }
}
