package org.example.backend.purchase;

import org.example.backend.purchase.dto.DealerBillItemDraft;
import org.example.backend.purchase.dto.DealerBillParseResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DealerBillTextParser {

    private static final Pattern INVOICE_NUMBER =
            Pattern.compile(
                    "Invoice\\s*No\\s*:\\s*(.+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern INVOICE_DATE =
            Pattern.compile(
                    "Invoice\\s*Date\\s*:\\s*(.+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern DEALER =
            Pattern.compile(
                    "Dealer\\s*:\\s*(.+)",
                    Pattern.CASE_INSENSITIVE
            );

    /*
     * Find a line ending with:
     *
     * MRP Discount DiscountedMRP Qty Total
     *
     * Example OCR:
     *
     * Royal Canin Maxi Adult 4 ~2,850 12% 22,508 5 212,540
     */
    private static final Pattern ITEM_LINE =
            Pattern.compile(
                    "^(.*?)\\s+" +
                    "(\\S*[0-9][0-9,.]*)\\s+" +
                    "(\\d{1,2})%\\s+" +
                    "(\\S*[0-9][0-9,.]*)\\s+" +
                    "(\\d+)\\s+" +
                    "(\\S*[0-9][0-9,.]*)\\s*$"
            );

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd-MMM-yyyy",
                    Locale.ENGLISH
            );

    public DealerBillParseResponse parse(
            String storedPath,
            String text
    ) {

        String invoiceNumber =
                extract(INVOICE_NUMBER, text);

        String dateText =
                extract(INVOICE_DATE, text);

        String dealerName =
                extract(DEALER, text);

        LocalDate billDate =
                dateText == null
                        ? null
                        : LocalDate.parse(
                                dateText,
                                DATE_FORMATTER
                        );

        List<DealerBillItemDraft> items =
                parseItems(text);

        return new DealerBillParseResponse(
                storedPath,
                dealerName,
                invoiceNumber,
                billDate,
                null,
                items,
                false
        );
    }

    private List<DealerBillItemDraft> parseItems(
            String text
    ) {

        List<DealerBillItemDraft> items =
                new ArrayList<>();

        String[] lines =
                text.split("\\R");

        PendingItem pending = null;

        for (String rawLine : lines) {

            String line = rawLine.trim();

            if (line.isBlank()) {
                continue;
            }

            if (shouldIgnore(line)) {
                continue;
            }

            Matcher matcher =
                    ITEM_LINE.matcher(line);

            if (matcher.matches()) {

                if (pending != null) {
                    items.add(pending.toDraft());
                }

                String productName =
                        matcher.group(1).trim();

                BigDecimal mrp =
                        parseMoney(matcher.group(2));

                BigDecimal discount =
                        new BigDecimal(
                                matcher.group(3)
                        );

                BigDecimal ocrDiscountedPrice =
                        parseMoney(matcher.group(4));

                int quantity =
                        Integer.parseInt(
                                matcher.group(5)
                        );

                BigDecimal ocrTotal =
                        parseMoney(matcher.group(6));

                BigDecimal discountedPrice =
                        calculateDiscountedPrice(
                                mrp,
                                discount
                        );

                BigDecimal total =
                        discountedPrice
                                .multiply(
                                        BigDecimal.valueOf(quantity)
                                )
                                .setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                );

                pending =
                        new PendingItem(
                                productName,
                                mrp,
                                discount,
                                discountedPrice,
                                quantity,
                                total,
                                ocrDiscountedPrice,
                                ocrTotal
                        );

            } else if (pending != null
                    && isProductContinuation(line)) {

                pending.name =
                        pending.name + " " + line;
            }
        }

        if (pending != null) {
            items.add(pending.toDraft());
        }

        return items;
    }

    private BigDecimal parseMoney(
            String raw
    ) {

        String normalized =
                raw
                        .replace(",", "")
                        .replaceAll(
                                "[^0-9.]",
                                ""
                        );

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    "Unable to parse money: " + raw
            );
        }

        return new BigDecimal(normalized);
    }

    private BigDecimal calculateDiscountedPrice(
            BigDecimal mrp,
            BigDecimal discountPercentage
    ) {

        BigDecimal discount =
                discountPercentage
                        .divide(
                                BigDecimal.valueOf(100),
                                6,
                                RoundingMode.HALF_UP
                        );

        return mrp
                .multiply(
                        BigDecimal.ONE.subtract(discount)
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private boolean shouldIgnore(
            String line
    ) {

        return line.startsWith("Product")
                || line.startsWith("Invoice No")
                || line.startsWith("Invoice Date")
                || line.startsWith("Dealer:")
                || line.startsWith("Invoice Total")
                || line.contains("Purchase Invoice");
    }

    private boolean isProductContinuation(
            String line
    ) {

        /*
         * Continuation lines like:
         *
         * KG
         * Veg 3 KG
         * Shampoo 200 ML
         * Biscuits 500 G
         */

        return !line.contains("%")
                && !line.startsWith("Invoice");
    }

    private String extract(
            Pattern pattern,
            String text
    ) {

        Matcher matcher =
                pattern.matcher(text);

        return matcher.find()
                ? matcher.group(1).trim()
                : null;
    }

    private static class PendingItem {

        private String name;

        private final BigDecimal mrp;
        private final BigDecimal discount;
        private final BigDecimal sellingPrice;
        private final Integer quantity;
        private final BigDecimal total;

        private final BigDecimal ocrSellingPrice;
        private final BigDecimal ocrTotal;

        private PendingItem(
                String name,
                BigDecimal mrp,
                BigDecimal discount,
                BigDecimal sellingPrice,
                Integer quantity,
                BigDecimal total,
                BigDecimal ocrSellingPrice,
                BigDecimal ocrTotal
        ) {
            this.name = name;
            this.mrp = mrp;
            this.discount = discount;
            this.sellingPrice = sellingPrice;
            this.quantity = quantity;
            this.total = total;
            this.ocrSellingPrice = ocrSellingPrice;
            this.ocrTotal = ocrTotal;
        }

        private DealerBillItemDraft toDraft() {

            boolean purchasePriceMismatch =
                    ocrSellingPrice.compareTo(sellingPrice) != 0;

            boolean totalMismatch =
                    ocrTotal.compareTo(total) != 0;

            boolean needsReview =
                    purchasePriceMismatch || totalMismatch;

            return new DealerBillItemDraft(
                    name,
                    mrp,
                    discount,
                    quantity,
                    sellingPrice,
                    total,
                    ocrSellingPrice,
                    ocrTotal,
                    needsReview
            );
        }
    }

}