package org.example.backend.purchase.extractor;

import org.example.backend.purchase.DealerBillOcrService;
import org.example.backend.purchase.DealerBillTextParser;
import org.example.backend.purchase.dto.DealerBillParseResponse;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class TesseractDealerBillExtractor
        implements DealerBillExtractor {

    private final DealerBillOcrService ocrService;
    private final DealerBillTextParser textParser;

    public TesseractDealerBillExtractor(
            DealerBillOcrService ocrService,
            DealerBillTextParser textParser
    ) {
        this.ocrService = ocrService;
        this.textParser = textParser;
    }

    @Override
    public DealerBillParseResponse extract(
            File billFile,
            String storedPath
    ) {

        String rawText =
                ocrService.extractText(
                        billFile.getAbsolutePath()
                );

        return textParser.parse(
                storedPath,
                rawText
        );
    }
}