package com.restaurant.billing_service.infrastructure.storage;

import com.restaurant.billing_service.presentation.exception.BillingGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class BillingStorageService {

    private final Path storageDirectory;

    public BillingStorageService(@Value("${billing.storage.directory:billing-documents}") String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public StoredBillingDocument store(UUID billingId, byte[] pdfContent) {
        if (billingId == null || pdfContent == null || pdfContent.length == 0) {
            throw new BillingGenerationException("No fue posible guardar el PDF del comprobante.", null);
        }

        try {
            Files.createDirectories(storageDirectory);
            String fileName = billingId + ".pdf";
            Path destination = storageDirectory.resolve(fileName);
            Files.write(destination, pdfContent);
            return new StoredBillingDocument(fileName, destination.toString());
        } catch (IOException ex) {
            throw new BillingGenerationException("Error al guardar el archivo PDF del comprobante.", ex);
        }
    }

    public byte[] load(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new BillingGenerationException("El comprobante no tiene un archivo asociado.", null);
        }

        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException ex) {
            throw new BillingGenerationException("No fue posible recuperar el archivo del comprobante.", ex);
        }
    }

    public record StoredBillingDocument(String fileName, String filePath) {
    }
}
