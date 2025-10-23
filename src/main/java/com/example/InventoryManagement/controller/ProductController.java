package com.example.InventoryManagement.controller;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.InventoryManagement.model.Product;
import com.example.InventoryManagement.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;
    private static final int PAGE_SIZE = 10;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // GET /products
    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "q", required = false) String q,
                       Model model) {
        Page<Product> products = service.listAll(page, PAGE_SIZE, q);
        model.addAttribute("products", products);
        model.addAttribute("q", q);
        return "products/list";
    }

    // GET /products/create
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/create";
    }

    // POST /products/create
    @PostMapping("/create")
    public String create(@ModelAttribute("product") @Valid Product product, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "products/create";
        }
        service.save(product);
        return "redirect:/products";
    }

    // GET /products/details/{id}
    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        Product p = service.get(id).orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", p);
        return "products/details";
    }

    // GET /products/edit/{id}
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product p = service.get(id).orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", p);
        return "products/edit";
    }

    // PUT /products/edit/{id}
    @PutMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("product") @Valid Product product, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "products/edit";
        }
        product.setId(id); // ensure id
        service.save(product);
        return "redirect:/products";
    }

    // GET /products/delete/{id}
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        Product p = service.get(id).orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", p);
        return "products/delete";
    }

    // DELETE /products/delete/{id}
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/products";
    }

    // Ajax: calc total -> GET /products/calc-total?quantity=...&unitPrice=...
    @GetMapping("/calc-total")
    @ResponseBody
    public Map<String, Integer> calcTotal(@RequestParam(defaultValue = "0") Integer quantity,
                                          @RequestParam(defaultValue = "0") Integer unitPrice) {
        int q = (quantity == null) ? 0 : quantity;
        int p = (unitPrice == null) ? 0 : unitPrice;
        return Map.of("total", q * p);
    }

    // Ajax: search -> GET /products/search?q=&page=
    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> search(@RequestParam(value = "q", required = false) String q,
                                      @RequestParam(value = "page", defaultValue = "0") int page) {
        Page<Product> p = service.listAll(page, PAGE_SIZE, q);
        return Map.of(
                "content", p.getContent(),
                "number", p.getNumber(),
                "totalPages", p.getTotalPages()
        );
    }

    // CSV Export -> GET /products/export
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportCsv(@RequestParam(value = "q", required = false) String q) throws UnsupportedEncodingException {
        List<Product> list = service.listAllNoPaging(q);
        ByteArrayInputStream stream = productsToCSV(list);
        String filename = "products.csv";
        InputStreamResource resource = new InputStreamResource(stream);

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(encodedFilename).build());
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    private ByteArrayInputStream productsToCSV(List<Product> products) {
        final String[] HEADER = {"ID", "商品名", "数量", "単価", "合計金額"};
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            // BOM を入れて Excel での文字化けを防ぐ（Windows Excel 対応）
            bw.write("\uFEFF");
            // header
            bw.write(String.join(",", HEADER));
            bw.newLine();

            for (Product p : products) {
                String line = String.format("%d,%s,%d,%d,%d",
                        p.getId() == null ? 0L : p.getId(),
                        csvEscape(p.getName()),
                        p.getQuantity() == null ? 0 : p.getQuantity(),
                        p.getUnitPrice() == null ? 0 : p.getUnitPrice(),
                        p.getTotalPrice() == null ? 0 : p.getTotalPrice());
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        String escaped = s.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + escaped + "\"";
        } else {
            return s;
        }
    }
}