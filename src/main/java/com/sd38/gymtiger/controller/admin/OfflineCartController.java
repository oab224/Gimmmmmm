
package com.sd38.gymtiger.controller.admin;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sd38.gymtiger.model.*;
import com.sd38.gymtiger.repository.ColorRepository;
import com.sd38.gymtiger.repository.MaterialRepository;
import com.sd38.gymtiger.repository.SizeRepository;
import com.sd38.gymtiger.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/tiger/pos")
public class OfflineCartController {

    @Autowired
    private OfflineCartService offlineCartService;

    @Autowired
    private BillService billService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRetailService customerRetailService;

    private Bill bill = new Bill();
    private Account currentUser = new Account();
    Date ngayhomnay = Date.valueOf(LocalDate.now());

    private String prodName = "";
    private String colorCode = "%%";
    private String matrCode = "%%";
    private String sizeName = "%%";

    private boolean tontaitronggio = false;
    private String thongbaotontai = "";

    private String kwds = "";

    @GetMapping()
    public String cart(Model model, HttpSession session, Principal principal,
                       @ModelAttribute("hoadoncho")Bill hoadoncho) {
        String currentUserName = principal.getName();
        currentUser = accountService.findFirstByEmail(currentUserName);

        List<Bill> listHdCho = new ArrayList<>();
        for (Bill k: billService.findAllByStatus(10)){
            if (k.getType()==1){
                listHdCho.add(k);
            }
        }
        List<Voucher> danhsachvoucher = voucherService.ActiveVoucher();
        if (bill.getId()!=null){
            List<BillDetail> danhsachHDCT = billService.getLstDetailByBillId(bill.getId());
            model.addAttribute("giohientai", danhsachHDCT);
        }
        // Get list customer
        List<CustomerRetail> customerList = customerRetailService.GetAll();
        if(bill.getCustomerRetail()==null){
            model.addAttribute("khUsername", "Chọn khách hàng");
            model.addAttribute("checkCustomer", false);
        }
        else {
            model.addAttribute("khUsername", bill.getCustomerRetail().getName());
        }
        // Get code voucher
        if(bill.getVoucher()==null || bill.getVoucher().getId() == null){
            model.addAttribute("StatusVoucher", 2);
            model.addAttribute("voucherInfo", "Chọn voucher");
        }
        else{
            var id = bill.getVoucher().getId();
            model.addAttribute("voucherId", id);
            var checkVoucher = model.getAttribute("StatusVoucher");
            if(checkVoucher == null){
                model.addAttribute("StatusVoucher", bill.getVoucher().getStatus());
            }
            model.addAttribute("voucherInfo", bill.getVoucher().getName());
            model.addAttribute("giatrivoucher", Double.parseDouble(String.valueOf(bill.getVoucher().getValue()))+"vnd");
        }

        hoadoncho = bill;

        model.addAttribute("giohientai",billService.getLstDetailByBillId(bill.getId()));
        model.addAttribute("hoadoncho", hoadoncho);
        model.addAttribute("listVoucher", danhsachvoucher);
        model.addAttribute("listCustomer", customerList);
        model.addAttribute("danhsachhoadon", listHdCho);
        model.addAttribute("soLuongHD", listHdCho.size());
        return "/admin/cart/offline-cart";
    }

    @RequestMapping("/addHD")
    public String taoHoaDon() {

            Bill hoadonmoi = new Bill();
            Integer soluong = billService.getAllBill().size()+1;
            hoadonmoi.setType(1);
            hoadonmoi.setCode("MHD" + soluong);
            hoadonmoi.setOrderDate(ngayhomnay);
            hoadonmoi.setPrice(BigDecimal.valueOf(0));
            hoadonmoi.setShippingFee(BigDecimal.valueOf(0));
            hoadonmoi.setTotalPrice(BigDecimal.valueOf(0));
            hoadonmoi.setCreateDate(ngayhomnay);
            hoadonmoi.setStatus(10);
            hoadonmoi.setVoucher(null);
            hoadonmoi.setEmployee(currentUser);
            hoadonmoi.setCustomer(null);
            billService.addBillPos(hoadonmoi);

            List<Bill> listHD2 = billService.findAllByStatus(10);
            bill = listHD2.get(listHD2.size() - 1);
        return "redirect:/tiger/pos";
    }

    @RequestMapping("/chonHD/{id}")
    public String chonBill(@PathVariable("id")Integer id){
        bill = billService.getOneBill(id);
        bill.setEmployee(currentUser);
        billService.updateBill(bill, id);
        return "redirect:/tiger/pos";
    }

    @RequestMapping("/xoa/{id}")
    public String xoaHoaDon(@PathVariable("id")Integer id){
        List<BillDetail> thuocvehdbihuy = billService.getLstDetailByBillId(id);
        for(BillDetail k: thuocvehdbihuy){
            huydonhang(k.getId());
        }
        Bill hd_bi_huy = billService.getOneBill(id);
        hd_bi_huy.setCancellationDate(Date.valueOf(LocalDate.now()));
        hd_bi_huy.setStatus(0);
        hd_bi_huy.setEmployee(currentUser);
        billService.updateBill(hd_bi_huy, id);
        bill = new Bill();
        return "redirect:/tiger/pos";
    }

    @RequestMapping("/thanhtoan")
    public String thanhtoan(@ModelAttribute("hoadoncho")Bill hoadon, RedirectAttributes redirectAttributes){
        var error = true;
        if (bill.getId()==null){
            return "redirect:/tiger/pos";
        }
        // Check voucher
        var voucher = hoadon.getVoucher();
        var voucherId = voucher.getId();
        if(voucherId != null){
            var check = voucherService.getOne(voucherId);
            if(check != null && check.getStatus() == 0) {
                redirectAttributes.addFlashAttribute("StatusVoucher", 0);
                return "redirect:/tiger/pos";
            }
        }
        Double tienSauVoucher = Double.parseDouble(String.valueOf(hoadon.getTotalPrice()));
        Double tienKhachDua = Double.parseDouble(String.valueOf(hoadon.getDiscountAmount()));
        Timestamp orderDate = new Timestamp(System.currentTimeMillis());

        Double tinhtien = tienKhachDua - tienSauVoucher;
        String chuThich = "Tiền thừa của khách là: "+ tinhtien;
        hoadon.setNote(chuThich);
        hoadon.setPaymentDate(ngayhomnay);
        hoadon.setConfirmationDate(ngayhomnay);
        hoadon.setCompletionDate(ngayhomnay);
        hoadon.setOrderDate(orderDate);
        hoadon.setShippingFee(BigDecimal.valueOf(0.0));
        hoadon.setCreateDate(bill.getCreateDate());
        hoadon.setUpdateDate(ngayhomnay);
        hoadon.setStatus(1);
        hoadon.setEmployee(currentUser);
        hoadon.setCustomer(bill.getCustomer());
        if(voucherId != null){
            hoadon.setVoucher(bill.getVoucher());
        }else{
            hoadon.setVoucher(null);
        }
        hoadon.setType(bill.getType());

        billService.addBillPos(hoadon);
        bill = new Bill();
        redirectAttributes.addFlashAttribute("StatusVoucher", 1);
        return "redirect:/tiger/pos";
    }

    //chi tiết sản phẩm trong giỏ
    @RequestMapping("/litSP")
    public String ProDlist(Model model){
        if(bill.getId()==null){
            return "redirect:/tiger/pos";
        }
        List<ProductDetail> detailList = productDetailService.locSpTaiQuay(
                "%"+prodName+"%",
                colorCode,
                matrCode,
                sizeName);

        model.addAttribute("danhsachSP", detailList);

        model.addAttribute("chatlieuL", materialRepository.findAll());
        model.addAttribute("mausacL", colorRepository.findAll());
        model.addAttribute("sizeL", sizeRepository.findAll());

        return "/admin/cart/side_form/product_detail_list";
    }

    @RequestMapping("/locSP")
    public String locSP(
            @RequestParam(defaultValue = "") String tukhoa,
            @RequestParam(defaultValue = "%%") String chatlieu,
            @RequestParam(defaultValue = "%%") String mausac,
            @RequestParam(defaultValue = "%%") String kichco
    ){
        prodName = tukhoa;
        colorCode = mausac;
        matrCode = chatlieu;
        sizeName = kichco;
        return "redirect:/tiger/pos/litSP";
    }

    @RequestMapping("/chon_de_them_vao_hd/{id}")
    public String ctsp_duoc_chon(@PathVariable("id")Integer id,Model model){
        if (bill.getId()==null){
            return "redirect:/tiger/pos";
        }
        ProductDetail detail = productDetailService.getOne(id);
        BillDetail billDetail = new BillDetail();
        billDetail.setProductDetail(detail);
        model.addAttribute("thongtinSP", billDetail);
        model.addAttribute("error", thongbaotontai);
        thongbaotontai = "";
        tontaitronggio = false;
        return "/admin/cart/side_form/chosen_product";
    }

    @RequestMapping("/them_vao_hd")
    public String themvaohoadon(@ModelAttribute("thongtinSP") BillDetail detail){
        List<BillDetail> kiemtratontai = billService.getLstDetailByBillId(bill.getId());
        ProductDetail productDetail = productDetailService.getOne(detail.getProductDetail().getId());

        for(BillDetail k: kiemtratontai){
            System.out.println(k.getStatus());
            if (k.getProductDetail().getId()==productDetail.getId() && k.getStatus()!=0){
                tontaitronggio = true;
            }
        }

        if (tontaitronggio){
            thongbaotontai = "Sản phẩm đã có trong giỏ";
            return "redirect:/tiger/pos/chon_de_them_vao_hd/"+productDetail.getId();
        }

        productDetail.setQuantity( productDetail.getQuantity() - detail.getQuantity() );

        System.out.println(productDetail.getProduct().getName()+" - "+detail.getQuantity()+" - "+productDetail.getId());
        //productDetailService.simplizedUpdate(productDetail.getId(), productDetail);

        detail.setBill(billService.getOneBill(bill.getId()));
        detail.setPrice(productDetail.getPriceDiscount());
        detail.setProductDetail(productDetail);
//        System.out.println(detail.getBill()+" - "+detail.getProductDetail().getProduct().getName()+" - "
//            +detail.getPrice()+" - "+detail.getQuantity()
//        );
        billService.addBillDetailPos(detail);
        tinhTongTien();
        return "redirect:/tiger/pos/chonHD/"+bill.getId();
    }

    @RequestMapping("/doi_so_luong_tai_quay/{id}")
    public String dieuchinhsoluongtronggio(
            @PathVariable("id")Integer id,
            @RequestParam("soluongmoi") Integer soluongmoi
    ){
        BillDetail hdct_dc_doi = billService.getOneBillDetail(id);
        ProductDetail spthaydoi = hdct_dc_doi.getProductDetail();

        Integer traVeSP = spthaydoi.getQuantity() + hdct_dc_doi.getQuantity() - soluongmoi;
        hdct_dc_doi.setQuantity(soluongmoi);
        spthaydoi.setQuantity(traVeSP);

        billService.updateBillDetail(hdct_dc_doi, id);
        productDetailService.simplizedUpdate(spthaydoi.getId(), spthaydoi);
        huyVcr();
        tinhTongTien();
        return "redirect:/tiger/pos/chonHD/"+bill.getId();
    };

    @RequestMapping("/xoa-hdtq/{id}")
    public String xoaBillDetail(@PathVariable("id")Integer id){
        huydonhang(id);
        huyVcr();
        tinhTongTien();
        return "redirect:/tiger/pos/chonHD/"+bill.getId();
    }
    //Khách và voucher
    @RequestMapping("/listKH_tai_quay")
    public String danhSachKhachHang(Model model){
        if (bill == null || bill.getId()==null){
            return "redirect:/tiger/pos";
        }
        if(model.containsAttribute("mess")){
            Integer mess = (Integer) model.getAttribute("mess");
        }
        List<Customer> listCustomer = customerService.findByKwds("%"+kwds+"%");
        model.addAttribute("listKH", listCustomer);
        return "/admin/cart/side_form/customers_list";
    }

    @RequestMapping("/tim_KH")
    public String locKH(@RequestParam(defaultValue = "")String tukhoa){
        kwds = tukhoa;
        return "redirect:/tiger/pos/listKH_tai_quay";
    }
    @RequestMapping("/chonKH/{id}")
    public String chonKH(@PathVariable("id")Integer id){
        Customer kh = customerService.findById(id);
        bill = billService.getOneBill(bill.getId());
        bill.setCustomer(kh);
        billService.addBillPos(bill);
        return "redirect:/tiger/pos/chonHD/"+bill.getId();
    };
    @RequestMapping("/listKH_tai_quay/huy_KH")
    public String huyKhTrongHd(){
        bill = billService.getOneBill(bill.getId());
        bill.setCustomerRetail(null);
        billService.addBillPos(bill);
        return "redirect:/tiger/pos/chonHD/"+bill.getId();
    }
    @RequestMapping("/chonVoucher/{id}")
    public String chonVcr(@PathVariable("id")Integer id){
        Voucher vcr = voucherService.getVoucherById(id);
        Bill them_vcr = billService.getOneBill(bill.getId());
        them_vcr.setVoucher(vcr);
        billService.addBillPos(them_vcr);
        tinhTongTien();
        return "redirect:/tiger/pos/chonHD/"+bill.getId();
    }
    @RequestMapping("/customer-retail/{id}")
    public String activeCustomer(@PathVariable("id")Integer id){
        CustomerRetail customerRetail = customerRetailService.getCustomerRetailById(id);
        Bill billEntity = billService.getOneBill(bill.getId());
        billEntity.setCustomerRetail(customerRetail);
        billService.addBillPos(billEntity);
        return "redirect:/tiger/pos/chonHD/"+bill.getId();
    }
    @RequestMapping("/listKH_tai_quay/huy_vcr")
    public String huyVcr(){
        Bill them_vcr = billService.getOneBill(bill.getId());
        them_vcr.setVoucher(null);
        billService.addBillPos(them_vcr);
        tinhTongTien();
        return "redirect:/tiger/pos/chonHD/"+bill.getId();
    }
    //public hỗ trợ
    private void tinhTongTien() {
        Double tongtien = 0.0;
        Double tinhKm = 0.0;
        List<BillDetail> listTrongGio = billService.getLstDetailByBillId(bill.getId());
        for (BillDetail k :listTrongGio){
            Integer soluong = k.getQuantity();
            Double giatien = Double.parseDouble(String.valueOf(k.getPrice()));
            Double nhanlen = soluong*giatien;
            tongtien = tongtien + nhanlen;
        }

        Bill hoadon = billService.getOneBill(bill.getId());
        if (hoadon.getVoucher()==null){
            tinhKm = tongtien;
        }
        else {
            tinhKm = tongtien - Double.parseDouble(String.valueOf(hoadon.getVoucher().getValue()));
        }

        hoadon.setPrice(BigDecimal.valueOf(tongtien));
        hoadon.setTotalPrice(BigDecimal.valueOf(tinhKm));
        billService.updateBill(hoadon, bill.getId());
    }

    private void huydonhang(Integer id) {
        BillDetail cthd_bi_xoa = billService.getOneBillDetail(id);
        ProductDetail sp_duoc_tra = cthd_bi_xoa.getProductDetail();

        sp_duoc_tra.setQuantity(
                sp_duoc_tra.getQuantity() + cthd_bi_xoa.getQuantity()
        );
        productDetailService.simplizedUpdate(sp_duoc_tra.getId(), sp_duoc_tra);
        billService.deleteBillDetail(cthd_bi_xoa);
    }

    @GetMapping("/print-pdf/{id}")
    public ResponseEntity<byte[]> inHoaDon(@PathVariable Integer id) throws DocumentException, IOException {
        Bill bill = billService.getOneBill(id);
        List<BillDetail> billDetails = billService.getLstDetailByBillId(id);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, baos);

        document.open();
        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 20, Font.BOLD);
        Font headerFont = new Font(bf, 12, Font.BOLD);
        Font normalFont = new Font(bf, 11, Font.NORMAL);

        // Tiêu đề
        Paragraph title = new Paragraph("Hóa Đơn", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Thông tin hóa đơn
        PdfPTable infoTable = new PdfPTable(new float[]{1, 4});
        infoTable.setWidthPercentage(100);
        infoTable.addCell(createCell("Mã hóa đơn:", headerFont, Element.ALIGN_LEFT, false));
        infoTable.addCell(createCell(bill.getCode(), normalFont, Element.ALIGN_LEFT, false));
        infoTable.addCell(createCell("Ngày:", headerFont, Element.ALIGN_LEFT, false));
        infoTable.addCell(createCell(LocalDate.now().toString(), normalFont, Element.ALIGN_LEFT, false));

        // Thêm thông tin khách hàng
        if (bill.getCustomer() != null) {
            infoTable.addCell(createCell("Khách hàng:", headerFont, Element.ALIGN_LEFT, false));
            infoTable.addCell(createCell(bill.getCustomer().getName(), normalFont, Element.ALIGN_LEFT, false));
        }

        // Thêm thông tin nhân viên tạo
        if (bill.getEmployee() != null) {
            infoTable.addCell(createCell("Nhân viên:", headerFont, Element.ALIGN_LEFT, false));
            infoTable.addCell(createCell(bill.getEmployee().getName(), normalFont, Element.ALIGN_LEFT, false));
        }

        infoTable.setSpacingAfter(20);
        document.add(infoTable);

        // Bảng chi tiết sản phẩm
        PdfPTable table = new PdfPTable(new float[] { 3, 1, 2, 2 });
        table.setWidthPercentage(100);
        table.addCell(createCell("Sản phẩm", headerFont, Element.ALIGN_CENTER, true));
        table.addCell(createCell("Số lượng", headerFont, Element.ALIGN_CENTER, true));
        table.addCell(createCell("Đơn giá", headerFont, Element.ALIGN_CENTER, true));
        table.addCell(createCell("Thành tiền", headerFont, Element.ALIGN_CENTER, true));

        for (BillDetail item : billDetails) {
            table.addCell(
                    createCell(item.getProductDetail().getProduct().getName(), normalFont, Element.ALIGN_LEFT, true));
            table.addCell(createCell(String.valueOf(item.getQuantity()), normalFont, Element.ALIGN_CENTER, true));
            table.addCell(createCell(String.format("%,.0f", item.getPrice()), normalFont, Element.ALIGN_RIGHT, true));
            table.addCell(
                    createCell(String.format("%,.0f", item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))),
                            normalFont, Element.ALIGN_RIGHT, true));
        }
        table.setSpacingAfter(20);
        document.add(table);

        // Thông tin tổng cộng
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        double voucherValue = bill.getVoucher() != null ? bill.getVoucher().getValue().doubleValue() : 0;
        double totalPayment = bill.getTotalPrice().doubleValue() - voucherValue;

        summaryTable.addCell(createCell("Tổng cộng:", headerFont, Element.ALIGN_LEFT, false));
        summaryTable.addCell(
                createCell(String.format("%,.0f VNĐ", bill.getTotalPrice()), normalFont, Element.ALIGN_RIGHT, false));
        summaryTable.addCell(createCell("Voucher:", headerFont, Element.ALIGN_LEFT, false));
        summaryTable
                .addCell(createCell(String.format("%,.0f VNĐ", voucherValue), normalFont, Element.ALIGN_RIGHT, false));
        summaryTable.addCell(createCell("Tổng thanh toán:", headerFont, Element.ALIGN_LEFT, false));
        summaryTable
                .addCell(createCell(String.format("%,.0f VNĐ", totalPayment), normalFont, Element.ALIGN_RIGHT, false));

        document.add(summaryTable);

        document.close();

        byte[] pdfBytes = baos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "hoa-don-" + bill.getCode() + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private PdfPCell createCell(String content, Font font, int alignment, boolean hasBorder) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(hasBorder ? Rectangle.BOX : Rectangle.NO_BORDER);
        return cell;
    }
}
