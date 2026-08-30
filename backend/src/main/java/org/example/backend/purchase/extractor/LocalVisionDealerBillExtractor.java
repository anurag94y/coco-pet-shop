package org.example.backend.purchase.extractor;

import org.example.backend.purchase.dto.DealerBillItemDraft;
import org.example.backend.purchase.dto.DealerBillParseResponse;
import org.example.backend.purchase.extractor.dto.VisionBillResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component("visionDealerBillExtractor")
public class LocalVisionDealerBillExtractor
        implements DealerBillExtractor {

    private static final BigDecimal HUNDRED =
            BigDecimal.valueOf(100);

    /*
     * One paisa tolerance.
     */
    private static final BigDecimal PRICE_TOLERANCE =
            new BigDecimal("0.01");

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    private final DealerBillDocumentSplitter documentSplitter;

    private final String model;

    public LocalVisionDealerBillExtractor(

            @Value("${dealer-bill.vision.base-url}")
            String baseUrl,

            @Value("${dealer-bill.vision.model}")
            String model,

            ObjectMapper objectMapper,

            DealerBillDocumentSplitter documentSplitter
    ) {

        this.restClient =
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .build();

        this.model = model;

        this.objectMapper =
                objectMapper;

        this.documentSplitter =
                documentSplitter;
    }

    @Override
    public DealerBillParseResponse extract(
            File billFile,
            String storedPath
    ) {

        if (!billFile.exists()) {
            throw new IllegalArgumentException(
                    "Dealer bill not found: "
                            + billFile.getAbsolutePath()
            );
        }

        List<File> chunks =
                documentSplitter.split(
                        billFile
                );

        if (chunks.isEmpty()) {
            throw new IllegalStateException(
                    "No readable pages found in dealer bill"
            );
        }

        List<VisionBillResponse> responses =
                new ArrayList<>();

        try {

            for (int i = 0;
                 i < chunks.size();
                 i++) {

                VisionBillResponse response =
                        extractChunk(
                                chunks.get(i),
                                i + 1,
                                chunks.size()
                        );

                responses.add(response);
            }

            return merge(
                    storedPath,
                    responses
            );

        } finally {

            cleanupTempFiles(
                    billFile,
                    chunks
            );
        }
    }

    private VisionBillResponse extractChunk(
            File image,
            int chunkNumber,
            int totalChunks
    ) {

        try {

            byte[] bytes =
                    Files.readAllBytes(
                            image.toPath()
                    );

            String base64 =
                    Base64.getEncoder()
                            .encodeToString(bytes);

            Map<String, Object> request =
                    Map.of(
                            "model",
                            model,

                            "stream",
                            false,

                            "format",
                            "json",

                            "options",
                            Map.of(
                                    "num_ctx",
                                    32768,

                                    "num_predict",
                                    8192,

                                    "temperature",
                                    0
                            ),

                            "messages",
                            List.of(
                                    Map.of(
                                            "role",
                                            "user",

                                            "content",
                                            prompt(
                                                    chunkNumber,
                                                    totalChunks
                                            ),

                                            "images",
                                            List.of(
                                                    base64
                                            )
                                    )
                            )
                    );

            JsonNode response =
                    restClient
                            .post()
                            .uri("/api/chat")
                            .body(request)
                            .retrieve()
                            .body(JsonNode.class);

            if (response == null) {
                throw new IllegalStateException(
                        "Empty response from vision model"
                );
            }

            validateOllamaResponse(
                    response,
                    chunkNumber
            );

            String content =
                    response
                            .path("message")
                            .path("content")
                            .asText();

            if (content == null
                    || content.isBlank()) {

                throw new IllegalStateException(
                        "Vision model returned empty JSON"
                );
            }

            try {

                return objectMapper.readValue(
                        content,
                        VisionBillResponse.class
                );

            } catch (Exception ex) {

                throw new IllegalStateException(
                        "Invalid JSON returned by vision model "
                                + "for chunk "
                                + chunkNumber
                                + ": "
                                + content,
                        ex
                );
            }

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Unable to read dealer bill image",
                    ex
            );
        }
    }

    private void validateOllamaResponse(
            JsonNode response,
            int chunkNumber
    ) {

        boolean done =
                response
                        .path("done")
                        .asBoolean();

        String doneReason =
                response
                        .path("done_reason")
                        .asText();

        if (!done) {

            throw new IllegalStateException(
                    "Vision model did not finish chunk "
                            + chunkNumber
            );
        }

        if ("length".equalsIgnoreCase(
                doneReason
        )) {

            throw new IllegalStateException(
                    "Vision model response was truncated "
                            + "for chunk "
                            + chunkNumber
            );
        }
    }

    private DealerBillParseResponse merge(
            String storedPath,
            List<VisionBillResponse> responses
    ) {

        String dealerName =
                firstNonBlank(
                        responses.stream()
                                .map(
                                        VisionBillResponse::
                                                dealerName
                                )
                                .toList()
                );

        String billNumber =
                firstNonBlank(
                        responses.stream()
                                .map(
                                        VisionBillResponse::
                                                billNumber
                                )
                                .toList()
                );

        var billDate =
                responses.stream()
                        .map(
                                VisionBillResponse::
                                        billDate
                        )
                        .filter(
                                Objects::nonNull
                        )
                        .findFirst()
                        .orElse(null);

        BigDecimal invoiceTotal =
                responses.stream()
                        .map(
                                VisionBillResponse::
                                        invoiceTotal
                        )
                        .filter(
                                Objects::nonNull
                        )
                        .findFirst()
                        .orElse(null);

        List<DealerBillItemDraft> items =
                new ArrayList<>();

        Set<String> dedupeKeys =
                new HashSet<>();

        for (VisionBillResponse response :
                responses) {

            if (response.items() == null) {
                continue;
            }

            for (
                    VisionBillResponse.VisionBillItem item :
                    response.items()
            ) {

                if (item == null
                        || item.name() == null
                        || item.name().isBlank()) {
                    continue;
                }

                DealerBillItemDraft draft =
                        validateItem(item);

                /*
                 * Because image chunks overlap,
                 * the same product row can appear
                 * in two consecutive chunks.
                 */
                String key =
                        buildDedupeKey(
                                draft
                        );

                if (dedupeKeys.add(key)) {
                    items.add(draft);
                }
            }
        }

        boolean itemNeedsReview =
                items.stream()
                        .anyMatch(
                                DealerBillItemDraft::
                                        needsReview
                        );

        boolean totalNeedsReview =
                validateInvoiceTotal(
                        invoiceTotal,
                        items
                );

        boolean needsReview =
                itemNeedsReview
                        || totalNeedsReview
                        || dealerName == null
                        || billNumber == null
                        || billDate == null
                        || items.isEmpty();

        return new DealerBillParseResponse(
                storedPath,
                dealerName,
                billNumber,
                billDate,
                invoiceTotal,
                items,
                needsReview
        );
    }

    private DealerBillItemDraft validateItem(
            VisionBillResponse.VisionBillItem item
    ) {

        BigDecimal extractedPurchasePrice =
                item.purchasePrice();

        BigDecimal extractedTotalPrice =
                item.totalPrice();

        boolean needsReview = false;

        BigDecimal calculatedPurchasePrice = null;
        BigDecimal calculatedTotalPrice = null;

        if (item.mrp() == null
                || item.discountPercentage() == null
                || item.quantity() == null) {

            needsReview = true;

        } else {

            calculatedPurchasePrice =
                    calculatePurchasePrice(
                            item.mrp(),
                            item.discountPercentage()
                    );

            calculatedTotalPrice =
                    calculatedPurchasePrice
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.quantity()
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            if (extractedPurchasePrice == null
                    || !approximatelyEqual(
                    calculatedPurchasePrice,
                    extractedPurchasePrice
            )) {

                needsReview = true;
            }

            if (extractedTotalPrice == null
                    || !approximatelyEqual(
                    calculatedTotalPrice,
                    extractedTotalPrice
            )) {

                needsReview = true;
            }
        }

        /*
         * Invoice values remain the source of truth.
         *
         * Calculated values are only used as a fallback
         * when the model could not extract a value.
         */
        BigDecimal finalPurchasePrice =
                extractedPurchasePrice != null
                        ? extractedPurchasePrice
                        : calculatedPurchasePrice;

        BigDecimal finalTotalPrice =
                extractedTotalPrice != null
                        ? extractedTotalPrice
                        : calculatedTotalPrice;

        return new DealerBillItemDraft(
                item.name(),
                item.mrp(),
                item.discountPercentage(),
                item.quantity(),
                finalPurchasePrice,
                finalTotalPrice,
                extractedPurchasePrice,
                extractedTotalPrice,
                needsReview
        );
    }

    private BigDecimal calculatePurchasePrice(
            BigDecimal mrp,
            BigDecimal discountPercentage
    ) {

        BigDecimal discount =
                discountPercentage
                        .divide(
                                HUNDRED,
                                6,
                                RoundingMode.HALF_UP
                        );

        return mrp
                .multiply(
                        BigDecimal.ONE
                                .subtract(discount)
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private boolean validateInvoiceTotal(
            BigDecimal invoiceTotal,
            List<DealerBillItemDraft> items
    ) {

        if (invoiceTotal == null) {
            return true;
        }

        BigDecimal calculatedTotal =
                items.stream()
                        .map(
                                DealerBillItemDraft::
                                        totalPrice
                        )
                        .filter(
                                Objects::nonNull
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return !approximatelyEqual(
                invoiceTotal,
                calculatedTotal
        );
    }

    private boolean approximatelyEqual(
            BigDecimal first,
            BigDecimal second
    ) {

        if (first == null
                || second == null) {
            return false;
        }

        return first
                .subtract(second)
                .abs()
                .compareTo(
                        PRICE_TOLERANCE
                ) <= 0;
    }

    private String buildDedupeKey(
            DealerBillItemDraft item
    ) {

        return normalize(
                item.name()
        )
                + "|"
                + normalize(
                item.mrp()
        )
                + "|"
                + normalize(
                item.dealerDiscountPercentage()
        )
                + "|"
                + item.quantity()
                + "|"
                + normalize(
                item.totalPrice()
        );
    }

    private String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private String normalize(
            BigDecimal value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .stripTrailingZeros()
                .toPlainString();
    }

    private String firstNonBlank(
            List<String> values
    ) {

        return values.stream()
                .filter(
                        Objects::nonNull
                )
                .map(
                        String::trim
                )
                .filter(
                        value ->
                                !value.isEmpty()
                )
                .findFirst()
                .orElse(null);
    }

    private void cleanupTempFiles(
            File original,
            List<File> chunks
    ) {

        for (File chunk : chunks) {

            /*
             * Never delete the uploaded
             * original invoice.
             */
            if (chunk.equals(original)) {
                continue;
            }

            try {
                Files.deleteIfExists(
                        chunk.toPath()
                );
            } catch (IOException ignored) {
            }
        }
    }

    private String prompt(
            int chunkNumber,
            int totalChunks
    ) {

        return """
                Extract product purchase information from this dealer invoice image.
                
                This is chunk %d of %d.
                
                Return ONLY compact valid JSON.
                Do not return Markdown.
                Do not use ```json blocks.
                Do not explain anything.
                
                JSON schema:
                
                {
                  "dealerName": "string or null",
                  "billNumber": "string or null",
                  "billDate": "yyyy-MM-dd or null",
                  "invoiceTotal": number or null,
                  "items": [
                    {
                      "name": "string",
                      "mrp": number,
                      "discountPercentage": number,
                      "purchasePrice": number,
                      "quantity": integer,
                      "totalPrice": number
                    }
                  ]
                }
                
                Rules:
                
                1. Extract every actual product row visible in this image.
                2. Preserve complete product names including package size.
                3. Combine wrapped product-name lines.
                4. Ignore table headers.
                5. Ignore repeated headers and footers.
                6. Do not include currency symbols or commas in numbers.
                7. mrp means original MRP per unit.
                8. purchasePrice means discounted dealer purchase price per unit.
                9. totalPrice means the row total.
                10. quantity must be an integer.
                11. Do not invent unreadable values.
                12. If invoice-level metadata is not visible in this chunk, return null.
                13. If invoice total is not visible, return null.
                14. If a row is visibly incomplete because it crosses a page/chunk boundary, do not guess missing numeric values.
                15. Return complete JSON only.
                """
                .formatted(
                        chunkNumber,
                        totalChunks
                );
    }
}