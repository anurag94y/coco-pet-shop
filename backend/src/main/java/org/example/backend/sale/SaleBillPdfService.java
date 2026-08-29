package org.example.backend.sale;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.backend.sale.dto.SaleItemResponse;
import org.example.backend.sale.dto.SaleResponse;
import org.example.backend.shop.Shop;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SaleBillPdfService {

    public byte[] generateBill(
            SaleResponse sale,
            Shop shop
    ) {

        try (
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Document document = new Document(PageSize.A4);

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            addShopDetails(document, shop);
            addBillDetails(document, sale);
            addItems(document, sale);
            addTotals(document, sale);
            addPaymentQr(document, sale, shop);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to generate bill PDF",
                    ex
            );
        }
    }

    private void addShopDetails(
            Document document,
            Shop shop
    ) throws DocumentException {

        Paragraph shopName = new Paragraph(
                shop.getName(),
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        18
                )
        );

        shopName.setAlignment(Element.ALIGN_CENTER);

        document.add(shopName);

        if (shop.getPhone() != null) {
            Paragraph phone = new Paragraph(
                    "Phone: " + shop.getPhone()
            );
            phone.setAlignment(Element.ALIGN_CENTER);
            document.add(phone);
        }

        if (shop.getAddress() != null) {
            Paragraph address = new Paragraph(
                    shop.getAddress()
            );
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
        }

        document.add(Chunk.NEWLINE);
    }

    private void addBillDetails(
            Document document,
            SaleResponse sale
    ) throws DocumentException {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        table.addCell(
                cell(
                        "Bill Number",
                        true
                )
        );

        table.addCell(
                cell(
                        sale.billNumber(),
                        false
                )
        );

        table.addCell(
                cell(
                        "Customer",
                        true
                )
        );

        table.addCell(
                cell(
                        sale.customerName(),
                        false
                )
        );

        table.addCell(
                cell(
                        "Date",
                        true
                )
        );

        table.addCell(
                cell(
                        sale.billDate().toString(),
                        false
                )
        );

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addItems(
            Document document,
            SaleResponse sale
    ) throws DocumentException {

        PdfPTable table = new PdfPTable(6);

        table.setWidthPercentage(100);

        table.setWidths(
                new float[]{
                        3f,
                        1f,
                        1.5f,
                        1.5f,
                        1.5f,
                        1.5f
                }
        );

        addHeader(table, "Product");
        addHeader(table, "Qty");
        addHeader(table, "MRP");
        addHeader(table, "Discount");
        addHeader(table, "Price");
        addHeader(table, "Total");

        for (SaleItemResponse item : sale.items()) {

            table.addCell(item.productName());
            table.addCell(
                    String.valueOf(item.quantity())
            );
            table.addCell(
                    money(item.mrp())
            );
            table.addCell(
                    item.discountPercentage() + "%"
            );
            table.addCell(
                    money(item.unitPrice())
            );
            table.addCell(
                    money(item.totalPrice())
            );
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addTotals(
            Document document,
            SaleResponse sale
    ) throws DocumentException {

        PdfPTable table = new PdfPTable(2);

        table.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        table.setWidthPercentage(45);

        table.addCell("Subtotal");
        table.addCell(
                money(sale.subtotal())
        );

        table.addCell("Discount");
        table.addCell(
                money(sale.discountAmount())
        );

        table.addCell(
                cell(
                        "Total",
                        true
                )
        );

        table.addCell(
                cell(
                        money(sale.totalAmount()),
                        true
                )
        );

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addPaymentQr(
            Document document,
            SaleResponse sale,
            Shop shop
    ) throws Exception {

        if (shop.getUpiId() == null
                || shop.getUpiId().isBlank()) {
            return;
        }

        String upiUrl = buildUpiUrl(
                shop,
                sale
        );

        BufferedImage qr =
                generateQrCode(upiUrl);

        Image image =
                Image.getInstance(
                        qr,
                        null
                );

        image.scaleToFit(130, 130);
        image.setAlignment(Element.ALIGN_CENTER);

        Paragraph paymentText =
                new Paragraph(
                        "Scan to Pay"
                );

        paymentText.setAlignment(
                Element.ALIGN_CENTER
        );

        document.add(paymentText);
        document.add(image);
    }

    private BufferedImage generateQrCode(
            String value
    ) throws Exception {

        QRCodeWriter qrCodeWriter =
                new QRCodeWriter();

        BitMatrix bitMatrix =
                qrCodeWriter.encode(
                        value,
                        BarcodeFormat.QR_CODE,
                        300,
                        300
                );

        return MatrixToImageWriter
                .toBufferedImage(bitMatrix);
    }

    private void addHeader(
            PdfPTable table,
            String value
    ) {

        PdfPCell cell = new PdfPCell(
                new Phrase(
                        value,
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD
                        )
                )
        );

        table.addCell(cell);
    }

    private PdfPCell cell(
            String value,
            boolean bold
    ) {

        Font font =
                bold
                        ? FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD
                        )
                        : FontFactory.getFont(
                                FontFactory.HELVETICA
                        );

        return new PdfPCell(
                new Phrase(value, font)
        );
    }

    private String money(
            BigDecimal amount
    ) {
        return "Rs. " + amount;
    }

    private String buildUpiUrl(
            Shop shop,
            SaleResponse sale
    ) {

        return "upi://pay"
                + "?pa=" + encode(shop.getUpiId())
                + "&pn=" + encode(shop.getName())
                + "&am=" + encode(sale.totalAmount().toPlainString())
                + "&cu=INR"
                + "&tn=" + encode(sale.billNumber());
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}