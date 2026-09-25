package com.example.crud.service;

import com.example.crud.domain.product.Product;
import com.example.crud.domain.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ViaCepService {

    private final RestTemplate restTemplate;
    private final ProductRepository productRepository;

    public ViaCepService(RestTemplate restTemplate, ProductRepository productRepository) {
        this.restTemplate = restTemplate;
        this.productRepository = productRepository;
    }

    public String checkAvailability(String productId, String cep) {
        Product product = productRepository.findById(productId)
                .orElseThrow(EntityNotFoundException::new);

        String normalizedCep = normalizeCep(cep);

        try {
            String url = "https://viacep.com.br/ws/{cep}/json/";
            ResponseEntity<ViaCepResponse> response =
                    restTemplate.getForEntity(url, ViaCepResponse.class, normalizedCep);

            ViaCepResponse address = response.getBody();

            if (!response.getStatusCode().is2xxSuccessful()
                    || address == null
                    || Boolean.TRUE.equals(address.erro())) {
                throw new ViaCepException("Não encontramos esse CEP. Confira os números e tente novamente.");
            }

            String city = address.localidade();
            String distributionCenter = product.getDistributionCenter();

            if (city == null || city.isBlank()) {
                throw new ViaCepException("A consulta do CEP não retornou uma cidade válida.");
            }

            if (distributionCenter == null || distributionCenter.isBlank()) {
                return "O CEP de " + city + " foi consultado, mas este produto não possui um centro de distribuição definido.";
            }

            if (city.trim().equalsIgnoreCase(distributionCenter.trim())) {
                return "Boa notícia! Entregamos este produto em " + city + ".";
            }

            return "No momento, este produto não está disponível para entrega em " + city + ".";

        } catch (ViaCepException e) {
            throw e;
        } catch (RestClientException e) {
            throw new ViaCepException("Não conseguimos consultar seu CEP agora. Tente novamente em instantes.");
        }
    }

    private String normalizeCep(String cep) {
        if (cep == null) {
            throw new ViaCepException("Informe seu CEP para consultar a disponibilidade de entrega.");
        }

        String normalized = cep.replaceAll("\\D", "");

        if (!normalized.matches("\\d{8}")) {
            throw new ViaCepException("O CEP informado parece incompleto ou inválido. Digite os 8 números do CEP.");

        }

        return normalized;
    }

    private record ViaCepResponse(
            String cep,
            String localidade,
            String uf,
            Boolean erro
    ) {
    }
}
