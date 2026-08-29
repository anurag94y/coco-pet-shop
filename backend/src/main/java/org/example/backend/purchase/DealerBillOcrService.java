package org.example.backend.purchase;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

@Service
public class DealerBillOcrService {

    private final ITesseract tesseract;

    public DealerBillOcrService(
            @Value("${ocr.tessdata-path}") String tessdataPath
    ) {
        Tesseract instance = new Tesseract();

        instance.setDatapath(tessdataPath);
        instance.setLanguage("eng");

        // Good starting point for invoice/table-like documents.
        instance.setPageSegMode(6);

        // Preserve spaces between columns a bit better.
        instance.setVariable(
                "preserve_interword_spaces",
                "1"
        );

        this.tesseract = instance;
    }

    public String extractText(String storedPath) {

        File file = new File(storedPath);

        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "Bill file not found: " + storedPath
            );
        }

        try {

            BufferedImage original =
                    ImageIO.read(file);

            if (original == null) {
                throw new IllegalArgumentException(
                        "Unsupported image file: " + storedPath
                );
            }

            BufferedImage processed =
                    preprocess(original);
            processed = addPadding(processed, 30);

            ImageIO.write(
                    processed,
                    "png",
                    new File("uploads/debug-ocr.png")
            );

            String value = tesseract.doOCR(processed);


            return tesseract.doOCR(processed);

        } catch (IOException | TesseractException ex) {
            throw new IllegalStateException(
                    "Unable to extract text from dealer bill",
                    ex
            );
        }
    }

    private BufferedImage addPadding(BufferedImage source, int padding) {
        BufferedImage result =
                new BufferedImage(
                        source.getWidth() + padding * 2,
                        source.getHeight() + padding * 2,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D graphics = result.createGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(
                0,
                0,
                result.getWidth(),
                result.getHeight()
        );

        graphics.drawImage(
                source,
                padding,
                padding,
                null
        );

        graphics.dispose();

        return result;
    }

    private BufferedImage preprocess(
            BufferedImage original
    ) {

        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage result =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D graphics =
                result.createGraphics();

        graphics.drawImage(
                original,
                0,
                0,
                null
        );

        graphics.dispose();

        boolean darkBackground =
                isDarkBackground(result);

        if (darkBackground) {
            invert(result);
        }

        return scale(result, 2.0);
    }

    private boolean isDarkBackground(
            BufferedImage image
    ) {

        long total = 0;

        int step = 20;

        for (int y = 0; y < image.getHeight(); y += step) {

            for (int x = 0; x < image.getWidth(); x += step) {

                int rgb =
                        image.getRGB(x, y);

                int value =
                        rgb & 0xFF;

                total += value;
            }
        }

        int sampledWidth =
                (image.getWidth() + step - 1) / step;

        int sampledHeight =
                (image.getHeight() + step - 1) / step;

        long samples =
                (long) sampledWidth * sampledHeight;

        double average =
                (double) total / samples;

        return average < 128;
    }

    private void invert(
            BufferedImage image
    ) {

        for (int y = 0; y < image.getHeight(); y++) {

            for (int x = 0; x < image.getWidth(); x++) {

                int rgb =
                        image.getRGB(x, y);

                int value =
                        rgb & 0xFF;

                int inverted =
                        255 - value;

                int newRgb =
                        (inverted << 16)
                                | (inverted << 8)
                                | inverted;

                image.setRGB(
                        x,
                        y,
                        newRgb
                );
            }
        }
    }

    private BufferedImage scale(
            BufferedImage source,
            double factor
    ) {

        int width =
                (int) (source.getWidth() * factor);

        int height =
                (int) (source.getHeight() * factor);

        BufferedImage scaled =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D graphics =
                scaled.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        graphics.drawImage(
                source,
                0,
                0,
                width,
                height,
                null
        );

        graphics.dispose();

        return scaled;
    }
}