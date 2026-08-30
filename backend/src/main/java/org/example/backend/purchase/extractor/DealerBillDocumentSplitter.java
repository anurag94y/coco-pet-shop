package org.example.backend.purchase.extractor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class DealerBillDocumentSplitter {

    private static final int MAX_IMAGE_HEIGHT = 2500;

    private static final int CHUNK_HEIGHT = 2200;

    private static final int OVERLAP = 200;

    private static final float PDF_DPI = 150;

    public List<File> split(File file) {

        String fileName =
                file.getName().toLowerCase();

        try {

            if (fileName.endsWith(".pdf")) {
                return splitPdf(file);
            }

            return splitImage(file);

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Unable to split dealer bill: "
                            + file.getAbsolutePath(),
                    ex
            );
        }
    }

    private List<File> splitPdf(File file)
            throws IOException {

        List<File> pages =
                new ArrayList<>();

        try (PDDocument document =
                     Loader.loadPDF(file)) {

            PDFRenderer renderer =
                    new PDFRenderer(document);

            for (int pageIndex = 0;
                 pageIndex < document.getNumberOfPages();
                 pageIndex++) {

                BufferedImage image =
                        renderer.renderImageWithDPI(
                                pageIndex,
                                PDF_DPI,
                                ImageType.RGB
                        );

                pages.addAll(
                        splitBufferedImage(
                                image,
                                "pdf-page-" + pageIndex
                        )
                );
            }
        }

        return pages;
    }

    private List<File> splitImage(File file)
            throws IOException {

        BufferedImage image =
                ImageIO.read(file);

        if (image == null) {
            throw new IllegalArgumentException(
                    "Unsupported image file: "
                            + file.getAbsolutePath()
            );
        }

        return splitBufferedImage(
                image,
                "invoice"
        );
    }

    private List<File> splitBufferedImage(
            BufferedImage image,
            String prefix
    ) throws IOException {

        if (image.getHeight()
                <= MAX_IMAGE_HEIGHT) {

            return List.of(
                    writeTempImage(
                            image,
                            prefix
                    )
            );
        }

        List<File> chunks =
                new ArrayList<>();

        int startY = 0;
        int chunkIndex = 0;

        while (startY < image.getHeight()) {

            int remaining =
                    image.getHeight() - startY;

            int height =
                    Math.min(
                            CHUNK_HEIGHT,
                            remaining
                    );

            BufferedImage chunk =
                    image.getSubimage(
                            0,
                            startY,
                            image.getWidth(),
                            height
                    );

            chunks.add(
                    writeTempImage(
                            chunk,
                            prefix
                                    + "-chunk-"
                                    + chunkIndex
                    )
            );

            if (startY + height
                    >= image.getHeight()) {
                break;
            }

            startY +=
                    CHUNK_HEIGHT - OVERLAP;

            chunkIndex++;
        }

        return chunks;
    }

    private File writeTempImage(
            BufferedImage image,
            String prefix
    ) throws IOException {

        File file =
                Files.createTempFile(
                        prefix,
                        ".png"
                ).toFile();

        ImageIO.write(
                image,
                "png",
                file
        );

        file.deleteOnExit();

        return file;
    }
}