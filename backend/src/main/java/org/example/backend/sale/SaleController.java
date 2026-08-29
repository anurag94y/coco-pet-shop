package org.example.backend.sale;

import jakarta.validation.Valid;
import org.example.backend.sale.dto.CreateSaleRequest;
import org.example.backend.sale.dto.SaleResponse;
import org.example.backend.sale.dto.SaleSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse createSale(
            @Valid @RequestBody CreateSaleRequest request
    ) {
        return saleService.createSale(request);
    }

    @GetMapping("/{saleId}")
    public SaleResponse getSale(
            @PathVariable Long saleId
    ) {
        return saleService.getSale(saleId);
    }

    @GetMapping("/customer/{customerId}")
    public Page<SaleSummaryResponse> getCustomerSales(
            @PathVariable Long customerId,
            Pageable pageable
    ) {
        return saleService.getCustomerSales(
                customerId,
                pageable
        );
    }

    @GetMapping("/shop/{shopId}")
    public Page<SaleSummaryResponse> getShopSales(
            @PathVariable Long shopId,
            Pageable pageable
    ) {
        return saleService.getShopSales(
                shopId,
                pageable
        );
    }

    @GetMapping(
            value = "/{saleId}/bill",
            produces = "application/pdf"
    )
    public ResponseEntity<byte[]> generateBill(
            @PathVariable Long saleId
    ) {

        byte[] pdf =
                saleService.generateBillPdf(
                        saleId
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"bill-"
                                + saleId
                                + ".pdf\""
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }
}