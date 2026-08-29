package org.example.backend.purchase.extractor;

import org.example.backend.purchase.dto.DealerBillParseResponse;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class DealerBillExtractionService {

    private final DealerBillExtractor extractor;

    public DealerBillExtractionService(
            DealerBillExtractor extractor
    ) {
        this.extractor = extractor;
    }

    public DealerBillParseResponse extract(
            String storedPath
    ) {

        File file = new File(storedPath);

        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "Bill file not found: " + storedPath
            );
        }

        return extractor.extract(
                file,
                storedPath
        );
    }
}