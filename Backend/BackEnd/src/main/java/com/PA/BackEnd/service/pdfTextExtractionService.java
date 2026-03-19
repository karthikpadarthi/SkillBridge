package com.PA.BackEnd.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class pdfTextExtractionService {

    public String extractText(MultipartFile file) {
        validatePdf(file);
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("Uploaded PDF does not contain readable text");
            }
            return text.trim();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse PDF resume: " + ex.getMessage());
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("resumeFile is required");
        }

        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        boolean isPdf = filename.endsWith(".pdf") || contentType.contains("pdf");
        if (!isPdf) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
    }
}

