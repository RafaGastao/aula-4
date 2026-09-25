package com.example.crud.controllers;

import com.example.crud.domain.category.RequestCategory;
import com.example.crud.domain.product.Product;
import com.example.crud.domain.product.ProductRepository;
import com.example.crud.domain.product.RequestProduct;
import com.example.crud.service.AddressSearch;
import com.example.crud.service.ViaCepService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductRepository repository;
    private final AddressSearch addressSearch;
    private final ViaCepService viaCepService;

    public ProductController(ProductRepository repository, AddressSearch addressSearch, ViaCepService viaCepService) {
        this.repository = repository;
        this.addressSearch = addressSearch;
        this.viaCepService = viaCepService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(repository.findAllByActiveTrue());
    }

    @GetMapping("/cep")
    public ResponseEntity<String> verifyAddress(@RequestParam String state,
                                                @RequestParam String city,
                                                @RequestParam String street) {
        return ResponseEntity.ok(addressSearch.searchAddress(state, city, street));
    }

    @GetMapping("/availability/{id}")
    public ResponseEntity<String> getProductAvailability(@PathVariable String id,
                                                         @RequestParam String cep) {
        return ResponseEntity.ok(viaCepService.checkAvailability(id, cep));
    }

    @GetMapping("/endpoint1")
    public ResponseEntity<List<Product>> getAllProducts1(@RequestParam String categoryAsParam) {
        return ResponseEntity.ok(repository.findAllByCategory(categoryAsParam));
    }

    @GetMapping("/endpoint2/{id}")
    public ResponseEntity<Optional<Product>> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(repository.findById(id));
    }

    @GetMapping("/endpoint3/top5byprice")
    public ResponseEntity<List<Product>> getAllProducts3() {
        var allProducts = repository.findAllByActiveTrue();

        List<Product> topFive = allProducts.stream()
                .sorted(Comparator.comparingInt(Product::getPrice).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(topFive);
    }

    @GetMapping("/category/{categoryAsPath}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @RequestHeader String categoryAsHeader,
            @PathVariable String categoryAsPath,
            @RequestBody @Valid RequestCategory categoryAsBody,
            @RequestParam String categoryAsParam
    ) {
        var allProducts = repository.findAllByActiveTrue();
        List<Product> filteredProducts = new ArrayList<>();

        for (Product product : allProducts) {
            if (categoryAsParam.equals(product.getCategory())) {
                filteredProducts.add(product);
            }
        }
        return ResponseEntity.ok(filteredProducts);
    }

    @PostMapping
    public ResponseEntity<Void> registerProduct(@RequestBody @Valid RequestProduct data) {
        repository.save(new Product(data));
        return ResponseEntity.ok().build();
    }

    @PutMapping
    @Transactional
    public ResponseEntity<Product> updateProduct(@RequestBody @Valid RequestProduct data) {
        Product product = repository.findById(data.id())
                .orElseThrow(EntityNotFoundException::new);

        product.setName(data.name());
        product.setPrice(data.price());
        product.setCategory(data.category());
        product.setDistributionCenter(data.distributionCenter());

        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        Product product = repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        product.setActive(false);
        return ResponseEntity.noContent().build();
    }
}
