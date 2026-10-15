package com.example.pdfstamp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestController
public class PdfStampController {
    private final PdfStampService service;

    public PdfStampController(PdfStampService service) {
        this.service = service;
    }

    @PostMapping(value = "/api/stamp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> stamp(@RequestPart("file") MultipartFile file,
            @RequestParam("stampText") String stampText,
            @RequestParam(value = "position", defaultValue = "TOP_RIGHT") String position) throws IOException {

        if (file.isEmpty() || file.getSize() > 20L * 1024 * 1024) {
            throw new IllegalArgumentException("Upload a non-empty PDF under 20 MB.");
        }
        byte[] bytes = file.getBytes();
        if (bytes.length < 5 || !Arrays.equals(Arrays.copyOfRange(bytes, 0, 5),
                "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
            throw new IllegalArgumentException("The uploaded file is not a PDF.");
        }

        byte[] stamped = service.stamp(bytes, stampText, PdfStampService.Position.parse(position));
        String supplied = file.getOriginalFilename() == null ? "document.pdf" : file.getOriginalFilename();
        String safe = supplied.replaceAll("[^a-zA-Z0-9._-]", "_").replaceAll("^\\.+", "");
        safe = safe.replaceAll("(?i)\\.pdf$", "");
        if (safe.isBlank()) safe = "document";
        String outputName = safe + "-stamped.pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(outputName).build());
        headers.setCacheControl("no-store");
        headers.add("X-Content-Type-Options", "nosniff");
        return new ResponseEntity<>(stamped, headers, HttpStatus.OK);
    }

    @ExceptionHandler({IllegalArgumentException.class, IOException.class,
            MaxUploadSizeExceededException.class})
    public ResponseEntity<Map<String, String>> onError(Exception ex) {
        String message = ex instanceof IllegalArgumentException ? ex.getMessage()
                : "Unable to process this PDF. Verify it is a valid, unprotected document.";
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
