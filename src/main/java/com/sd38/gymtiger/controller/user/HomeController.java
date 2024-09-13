package com.sd38.gymtiger.controller.user;

import com.sd38.gymtiger.dto.common.ProductAndValueDiscountDto;
import com.sd38.gymtiger.dto.common.impl.ProductAndValueDiscountDtoImpl;
import com.sd38.gymtiger.model.*;
import com.sd38.gymtiger.response.ProductDetailResponse;
import com.sd38.gymtiger.response.ProductDiscountHomeResponse;
import com.sd38.gymtiger.response.SizeDetailResponse;
import com.sd38.gymtiger.service.*;
import com.sd38.gymtiger.service.user.ProductViewService;
import com.sd38.gymtiger.service.user.UserColorService;
import com.sd38.gymtiger.service.user.UserSizeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
public class HomeController {
    @Autowired
    private ProductViewService productViewService;

    @Autowired
    private UserSizeService userSizeService;

    @Autowired
    private UserColorService userColorService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FormService formService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ColorService colorService;

    @Autowired
    private SizeService sizeService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @GetMapping("/home")
    public String home(@RequestParam(defaultValue = "0") int page,
                       Model model,
                       Principal principal,
                       HttpSession session) {
        if (principal != null) {
            Customer customer = customerService.findByEmail(principal.getName());
            session.setAttribute("username", customer.getName());
            session.setAttribute("role", customer.getRole().getName());
            Cart cart = cartService.getCart(principal.getName());
            SessionCart sessionCart = (SessionCart) session.getAttribute("sessionCart");
            if (sessionCart != null) {
                if (sessionCart.getCartDetails() != null) {
                    cartService.combineCart(sessionCart, principal.getName());
                    session.removeAttribute("sessionCart");
                }
            }
            if (cart != null) {
                session.setAttribute("totalItems", cart.getTotalItems());
            }
        }
        List<ProductDiscountHomeResponse> listProducAndDiscountHomeResponse = productViewService.getAllProductAndProductDiscountHomeResponse();
        model.addAttribute("listProducAndDiscountHomeResponse", productViewService.setPriceDiscount(listProducAndDiscountHomeResponse));
//        model.addAttribute("listProducAndDiscountHomeResponse", listProducAndDiscountHomeResponse);
        List<ProductDiscountHomeResponse> listProductDiscountHomeResponse = productViewService.getAllProductDiscountHomeResponse();
        model.addAttribute("listProductDiscountHomeResponse", productViewService.setPriceDiscount(listProductDiscountHomeResponse));

        return "/user/home";
    }

    @GetMapping("/shop")
    public String shop(Model model,
                       @RequestParam(defaultValue = "0") int page) {
        List<ProductDiscountHomeResponse> listProductDiscountHomeResponse = productViewService.getAllProductAndProductDiscountShopResponse();
        Page<ProductDiscountHomeResponse> pageProductDiscountHomeResponse = productViewService.convertlistToPage(productViewService.setPriceDiscount(listProductDiscountHomeResponse), page);
        model.addAttribute("pageProductShopResponse", pageProductDiscountHomeResponse);
        List<Material> listMaterial = materialService.getAll();
        List<Category> listCategory = categoryService.getAll();
        List<Form> listForm = formService.getAll();
        List<Brand> listBrand = brandService.getAll();
        List<Color> listColor = colorService.getAll();
        List<Size> listSize = sizeService.getAll();
        model.addAttribute("priceMin", 0);
        model.addAttribute("priceMax", 10000000);
        model.addAttribute("listMaterial", listMaterial);
        model.addAttribute("listForm", listForm);
        model.addAttribute("listBrand", listBrand);
        model.addAttribute("listCategory", listCategory);
        model.addAttribute("listColor", listColor);
        model.addAttribute("listSize", listSize);
        return "/user/shop";
    }

    @GetMapping("/search")
    public String searchProductResponse(@RequestParam(required = false) String productNameSearch,
                                        @RequestParam(required = false) Integer sort,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(required = false) List<Integer> listBrandId,
                                        @RequestParam(required = false) List<Integer> listMaterialId,
                                        @RequestParam(required = false) List<Integer> listCategoryId,
                                        @RequestParam(required = false) List<Integer> listFormId,
//                                        @RequestParam(required = false) Integer price,
                                        @RequestParam(required = false) List<Integer> listSizeId,
                                        @RequestParam(required = false) List<Integer> listColorId,
                                        @RequestParam(required = false) BigDecimal priceMin,
                                        @RequestParam(required = false) BigDecimal priceMax,
                                        @RequestParam(defaultValue = "0") int page,
                                        Model model) {
        if ("".equals(productNameSearch) && status == null && sort == null && Objects.equals(priceMin, BigDecimal.valueOf(0)) && Objects.equals(priceMax, BigDecimal.valueOf(10000000)) && listBrandId == null && listMaterialId == null && listCategoryId == null && listFormId == null && listSizeId == null && listColorId == null) {
            return "redirect:/shop";
        }
//        BigDecimal priceMax = productViewService.getPriceMaxBySelected(price);
//        BigDecimal priceMin = productViewService.getPriceMinBySelected(price);
        List<Integer> listColorIdNew = productViewService.removeNullValueInList(listColorId);
        List<Integer> listSizeIdNew = productViewService.removeNullValueInList(listSizeId);
        List<Integer> listBrandIdNew = productViewService.removeNullValueInList(listBrandId);
        List<Integer> listCategoryIdNew = productViewService.removeNullValueInList(listCategoryId);
        List<Integer> listMaterialIdNew = productViewService.removeNullValueInList(listMaterialId);
        List<Integer> listFormIdNew = productViewService.removeNullValueInList(listFormId);
        List<ProductDiscountHomeResponse> listProductDiscountHomeResponse = new ArrayList<>();
        if (sort == null) {
            listProductDiscountHomeResponse = productViewService.searchProductAndProductDiscountShopResponse(listBrandIdNew, listCategoryIdNew, listFormIdNew, listMaterialIdNew, listSizeIdNew, listColorIdNew, productNameSearch, priceMax, priceMin, status);
        } else if (sort == 1) {
            listProductDiscountHomeResponse = productViewService.searchProductAndProductDiscountDescShopResponse(listBrandIdNew, listCategoryIdNew, listFormIdNew, listMaterialIdNew, listSizeIdNew, listColorIdNew, productNameSearch, priceMax, priceMin, status);
        } else if (sort == 2) {
            listProductDiscountHomeResponse = productViewService.searchProductAndProductDiscountAscShopResponse(listBrandIdNew, listCategoryIdNew, listFormIdNew, listMaterialIdNew, listSizeIdNew, listColorIdNew, productNameSearch, priceMax, priceMin, status);
        } else if (sort == 0) {
            listProductDiscountHomeResponse = productViewService.searchOnlyProductDiscountShopResponse(listBrandIdNew, listCategoryIdNew, listFormIdNew, listMaterialIdNew, listSizeIdNew, listColorIdNew, productNameSearch, priceMax, priceMin);
        }
        List<ProductDiscountHomeResponse> listProductDiscountHomeResponseNew = productViewService.setPriceDiscount(listProductDiscountHomeResponse);
        Page<ProductDiscountHomeResponse> pageProductDiscountHomeResponse = productViewService.convertlistToPage(listProductDiscountHomeResponseNew, page);
        model.addAttribute("pageProductShopResponse", pageProductDiscountHomeResponse);
        List<Material> listMaterial = materialService.getAll();
        List<Category> listCategory = categoryService.getAll();
        List<Form> listForm = formService.getAll();
        List<Brand> listBrand = brandService.getAll();
        List<Color> listColor = colorService.getAll();
        List<Size> listSize = sizeService.getAll();
        StringBuilder pageListSize = productViewService.pageListSize(listSizeIdNew);
        StringBuilder pageListColor = productViewService.pageListColor(listColorIdNew);
        StringBuilder pageListBrand = productViewService.pageListBrand(listBrandIdNew);
        StringBuilder pageListCategory = productViewService.pageListCategory(listCategoryIdNew);
        StringBuilder pageListMaterial = productViewService.pageListMaterial(listMaterialIdNew);
        StringBuilder pageListForm = productViewService.pageListForm(listFormIdNew);
        model.addAttribute("pageListColor", pageListColor);
        model.addAttribute("pageListSize", pageListSize);
        model.addAttribute("pageListBrand", pageListBrand);
        model.addAttribute("pageListCategory", pageListCategory);
        model.addAttribute("pageListMaterial", pageListMaterial);
        model.addAttribute("pageListForm", pageListForm);
        model.addAttribute("listMaterial", listMaterial);
        model.addAttribute("listCategory", listCategory);
        model.addAttribute("listForm", listForm);
        model.addAttribute("listBrand", listBrand);
        model.addAttribute("listColor", listColor);
        model.addAttribute("listSize", listSize);
        model.addAttribute("priceMax", priceMax);
        model.addAttribute("priceMin", priceMin);
        model.addAttribute("listBrandId", listBrandId);
        model.addAttribute("listMaterialId", listMaterialId);
        model.addAttribute("listCategoryId", listCategoryId);
        model.addAttribute("listFormId", listFormId);
        model.addAttribute("listSizeId", listSizeId);
        model.addAttribute("listColorId", listColorId);
        model.addAttribute("sort", sort);
//        model.addAttribute("price", price);
        model.addAttribute("status", status);
        model.addAttribute("productNameSearch", productNameSearch);
        return "user/shop";
    }

    @GetMapping("/product-detail/quantity-and-price")
    @ResponseBody
    public ProductDetailResponse getProductDetailQuantityAndPrice(@RequestParam Integer productId, @RequestParam Integer sizeId, @RequestParam Integer colorId) {
        return productViewService.getProductDetailQuantityAndPrice(productId, sizeId, colorId);
    }

    @GetMapping("/product-detail/{productId}")
    public String productDetail(@PathVariable Integer productId, Model model) {

        // 1. Lấy thông tin chi tiết của sản phẩm dựa trên productId
        Product product = productViewService.getOne(productId);
        // 1.1. Lấy số lượng sản phẩm còn tồn:
        int numberProductDetail = 0;
        if(product != null) {
            var lstProductDetail = product.getListProductDetail();
            for(var item : lstProductDetail) {
                numberProductDetail += item.getQuantity();
            }
        }
        // 2. Lấy danh sách sản phẩm gợi ý (có thể bạn cũng thích)
        List<ProductDiscountHomeResponse> listProductYouMayLikeResponse = productViewService.getRandomProductAndProductDiscount();
        model.addAttribute("listProductYouMayLikeResponse", listProductYouMayLikeResponse);

        // 3. Lấy giá trị giảm giá (nếu có) và giá cao nhất/thấp nhất của sản phẩm
        Float value = productViewService.getValueDiscountByProductId(productId);
        BigDecimal priceMax = productViewService.getPriceMaxResponseByProductId(productId);
        BigDecimal priceMin = productViewService.getPriceMinResponseByProductId(productId);
        ProductAndValueDiscountDto productAndValueDiscountDto = productViewService.getProductAndValueDiscount(productId);

        // 4. Tính toán giá sau khi giảm (nếu có)
        if (productAndValueDiscountDto.getValue() != null) {
            // Nếu có khuyến mãi theo phần trăm, tính giá sau giảm dựa trên giá trị khuyến mãi
            BigDecimal priceDiscountMax = productViewService.calculatePriceToPriceDiscount(priceMax, productAndValueDiscountDto.getValue());
            BigDecimal priceDiscountMin = productViewService.calculatePriceToPriceDiscount(priceMin, productAndValueDiscountDto.getValue());
            model.addAttribute("priceDiscountMax", priceDiscountMax);
            model.addAttribute("priceDiscountMin", priceDiscountMin);
        } else {
            // Nếu không có khuyến mãi theo phần trăm, lấy giá giảm giá cao nhất/thấp nhất từ service
            BigDecimal priceDiscountMax = productViewService.getPriceDiscountMaxResponseByProductId(productId);
            BigDecimal priceDiscountMin = productViewService.getPriceDiscountMinResponseByProductId(productId);
            model.addAttribute("priceDiscountMax", priceDiscountMax);
            model.addAttribute("priceDiscountMin", priceDiscountMin);
        }

        // 5. Lấy danh sách kích cỡ và màu sắc của sản phẩm
        var listProductSize = productViewService.getAllSizeDetailResponse(productId);
        //5.1 Sắp xếp kích cỡ nhỏ tới lớn
        listProductSize.sort(Comparator.comparing(SizeDetailResponse::getName));
        var listProductColor = productViewService.getAllColorDetailResponse(productId);

        // 6. Lấy danh sách hình ảnh của sản phẩm
        List<Image> listImage = imageService.getAllImageByProductId(productId);
        model.addAttribute("listImage", listImage);

        // 7. Lấy hình ảnh active của sản phẩm
        model.addAttribute("imageActive", imageService.getImageActiveByProductId(productId));

        // 8. Thêm các thông tin vào model để hiển thị trên view
        model.addAttribute("product", product);
        model.addAttribute("listProductColor", listProductColor);
        model.addAttribute("listProductSize", listProductSize);
        model.addAttribute("priceMax", priceMax);
        model.addAttribute("priceMin", priceMin);
        model.addAttribute("numberProductDetail", numberProductDetail);
        // 9. Chuyển đổi và thêm thông tin giảm giá vào model
        var discount = ProductAndValueDiscountDtoImpl.toResponse(productAndValueDiscountDto);
        model.addAttribute("discount", discount);
        model.addAttribute("value", value);

        // 10. Trả về tên view để hiển thị chi tiết sản phẩm
        return "/user/detail";
    }

    @GetMapping("/contact")
    public String contact() {
        return "/user/contact";
    }

    @GetMapping("/about")
    public String about() {
        return "/user/about";
    }
}
