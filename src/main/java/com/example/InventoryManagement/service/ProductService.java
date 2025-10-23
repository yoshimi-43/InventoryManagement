package com.example.InventoryManagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.InventoryManagement.model.Product;
import com.example.InventoryManagement.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository repo;
    private static final Sort DEFAULT_SORT = Sort.by("id").descending();

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Page<Product> listAll(int page, int size, String q) {
        Pageable pageable = PageRequest.of(page, size, DEFAULT_SORT);
        if (q == null || q.isBlank()) {
            return repo.findAll(pageable);
        } else {
            return repo.findByNameContainingIgnoreCase(q.trim(), pageable);
        }
    }

    public Optional<Product> get(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public Product save(Product p) {
        return repo.save(p);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<Product> listAllNoPaging(String q) {
        if (q == null || q.isBlank()) {
            return repo.findAll(DEFAULT_SORT);
        } else {
            return repo.findByNameContainingIgnoreCase(q.trim(), Pageable.unpaged()).getContent();
        }
    }
}