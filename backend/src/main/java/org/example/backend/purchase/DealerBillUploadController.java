package org.example.backend.purchase;

import org.example.backend.dealer.DealerPaymentService;
import org.example.backend.purchase.dto.DealerBillOcrResponse;
import org.example.backend.purchase.dto.DealerBillParseResponse;
import org.example.backend.purchase.dto.DealerBillUploadResponse;
import org.example.backend.purchase.extractor.DealerBillExtractionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/purchases")
public class DealerBillUploadController {

    private final DealerBillStorageService storageService;
    private final DealerBillParserService dealerBillParserService;
    private final DealerBillExtractionService dealerBillExtractionService;

    public DealerBillUploadController(
            DealerBillStorageService storageService,
            DealerBillParserService dealerBillParserService,
            DealerBillExtractionService dealerBillExtractionService
    ) {
        this.storageService = storageService;
        this.dealerBillParserService = dealerBillParserService;
        this.dealerBillExtractionService = dealerBillExtractionService;
    }

    @PostMapping(
            value = "/bill-upload",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public DealerBillUploadResponse uploadBill(
            @RequestPart("file")
            MultipartFile file
    ) {
        return storageService.store(file);
    }

//    @PostMapping("/bill-parse")
//    public DealerBillOcrResponse parseBill(
//            @RequestParam String storedPath
//    ) {
//        return dealerBillParserService.parse(storedPath);
//    }

    @PostMapping("/bill-parse")
    public DealerBillParseResponse parseBill(
            @RequestParam String storedPath
    ) {
        return dealerBillExtractionService.extract(
                storedPath
        );
    }
}