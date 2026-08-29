package org.example.backend.purchase;

import org.example.backend.purchase.dto.DealerBillUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class DealerBillStorageService {

    private static final Path BILL_DIRECTORY =
            Path.of("uploads", "dealer-bills");

    public DealerBillUploadResponse store(
            MultipartFile file
    ) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Dealer bill file cannot be empty"
            );
        }

        try {
            Files.createDirectories(BILL_DIRECTORY);

            String originalName =
                    file.getOriginalFilename();

            String extension = "";

            if (originalName != null
                    && originalName.contains(".")) {

                extension =
                        originalName.substring(
                                originalName.lastIndexOf(".")
                        );
            }

            String storedFileName =
                    UUID.randomUUID() + extension;

            Path target =
                    BILL_DIRECTORY.resolve(
                            storedFileName
                    );

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return new DealerBillUploadResponse(
                    originalName,
                    target.toString()
            );

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to store dealer bill",
                    ex
            );
        }
    }
}