package org.example.backend.purchase.extractor;

import org.example.backend.purchase.dto.DealerBillParseResponse;

import java.io.File;

public interface DealerBillExtractor {

    DealerBillParseResponse extract(
            File billFile,
            String storedPath
    );
}