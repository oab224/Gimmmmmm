package com.sd38.gymtiger.controller.admin;

import com.sd38.gymtiger.model.Image;
import com.sd38.gymtiger.model.Product;
import com.sd38.gymtiger.service.ImageService;
import com.sd38.gymtiger.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/tiger")
public class ImageController {
    @Autowired
    private ImageService imageService;

    @Autowired
    private ProductService productService;

    @GetMapping("/product/{productId}/image")
    public String getImage(@PathVariable Integer productId,
                           Model model,
                           @RequestParam(defaultValue = "0") int page) {
        Page<Image> pageImage = imageService.getImageByProduct(page, productId);
        Product product = productService.getOne(productId);
        model.addAttribute("product", product);
        model.addAttribute("pageImage", pageImage);
        model.addAttribute("productId", product.getId());
        return "admin/image/image";
    }

    @PostMapping("/image/add")
    public String add(@RequestParam(required = false) Integer productId,
                      @RequestParam List<MultipartFile> images,
                      RedirectAttributes redirectAttributes) {
        imageService.add(productId, images);
        redirectAttributes.addFlashAttribute("mess", "Thêm dữ liệu thành công");
        return "redirect:/tiger/product/" + productId + "/image";
    }

    @PostMapping("/image/update/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam(required = false) Integer productId,
                         @RequestParam MultipartFile image,
                         RedirectAttributes redirectAttributes) {
        imageService.update(id, productId, image);
        redirectAttributes.addFlashAttribute("mess", "Sửa dữ liệu thành công");
        return "redirect:/tiger/product/" + productId + "/image";
    }


    @GetMapping("/image/detail/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Image image = imageService.detail(id);
        model.addAttribute("imageDetail", image);
        return "admin/image/image-detail";
    }

    @PostMapping("/image/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Integer productId = imageService.getProductDetailByIdImage(id);
        imageService.delete(id);
        redirectAttributes.addFlashAttribute("mess", "Xóa dữ liệu thành công");
        return "redirect:/tiger/product/" + productId + "/image";
    }

    @GetMapping("/product/{productId}/image/view-restore")
    public String viewRestore(@PathVariable Integer productId,
                              @RequestParam(defaultValue = "0") Integer page, Model model) {
        Page<Image> pageImage = imageService.getAllDeletedByProductId(productId, page);
        model.addAttribute("pageImage", pageImage);
        model.addAttribute("productId", productId);
        return "admin/image/image-restore";
    }

    @PostMapping("/product/{productId}/image/restore/{id}")
    public String restore(@PathVariable Integer productId,
                          @PathVariable Integer id,
                          RedirectAttributes redirectAttributes) {
        imageService.restore(id);
        redirectAttributes.addFlashAttribute("mess", "Khôi phục dữ liệu thành công");
        return "redirect:/tiger/product/" + productId + "/image/view-restore";
    }

}
