package com.moin.remittance.service.exchange.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moin.remittance.domain.dto.exchange.ExchangeRateDTO;
import com.moin.remittance.service.exchange.ExchangeService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.util.HashMap;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    private static final String EXCHANGE_RATE_API_URL = "https://crix-api-cdn.upbit.com/v1/forex/recent";

    @Override
    public HashMap<String, ExchangeRateDTO> fetchExchangeRate(String codes) {
        String postCodes = "";
        if ("JPY".equals(codes)) {
            postCodes = "FRX.KRWJPY";
        } else if ("USD".equals(codes)) {
            postCodes = "FRX.KRWUSD";
        }

        // Build the complete URI with the query parameter
        URI uri = UriComponentsBuilder.fromHttpUrl(EXCHANGE_RATE_API_URL)
                .queryParam("codes", postCodes)
                .build()
                .toUri();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HashMap<String, ExchangeRateDTO> exchangeRateInfoHashMap = new HashMap<>();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode firstNode = root.get(0);
            ExchangeRateDTO rateDTO = new ExchangeRateDTO();
            rateDTO.setCode(codes);
            rateDTO.setCurrencyCode(firstNode.path("currencyCode").asText());
            rateDTO.setBasePrice(firstNode.path("basePrice").doubleValue());
            rateDTO.setCurrencyUnit(firstNode.path("recurrenceCount").intValue());

            exchangeRateInfoHashMap.put(rateDTO.getCode(), rateDTO);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return exchangeRateInfoHashMap;
    }
}
