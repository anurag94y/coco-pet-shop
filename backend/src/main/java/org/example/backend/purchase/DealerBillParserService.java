package org.example.backend.purchase;

import org.example.backend.purchase.dto.DealerBillOcrResponse;
import org.example.backend.purchase.dto.DealerBillParseResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DealerBillParserService {

    private final DealerBillOcrService ocrService;

    public DealerBillParserService(
            DealerBillOcrService ocrService
    ) {
        this.ocrService = ocrService;
    }

    public DealerBillOcrResponse parse(String storedPath) {

        String extractedText =
                ocrService.extractText(storedPath);

        return new DealerBillOcrResponse(
                storedPath,
                extractedText
        );
    }
}