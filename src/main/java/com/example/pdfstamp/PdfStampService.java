package com.example.pdfstamp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class PdfStampService {
    public enum Position {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER;

        public static Position parse(String value) {
            try {
                return Position.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new IllegalArgumentException("Unknown stamp position.");
            }
        }
    }

    public byte[] stamp(byte[] sourcePdf, String requestedText, Position position) throws IOException {
        if (requestedText == null || requestedText.isBlank() || requestedText.length() > 100) {
            throw new IllegalArgumentException("Stamp text must contain 1–100 characters.");
        }
        String stampText = requestedText.trim();
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        try {
            font.encode(stampText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Stamp contains unsupported characters. Use Latin text and standard punctuation.");
        }

        try (PDDocument document = Loader.loadPDF(sourcePdf);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (document.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("The PDF has no pages.");
            }
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("Encrypted PDFs are not supported.");
            }
            PDPage page = document.getPage(0);
            if (page.getRotation() % 360 != 0) {
                throw new IllegalArgumentException("Rotated pages are not supported in this prototype.");
            }
            PDRectangle box = page.getCropBox();
            float fontSize = 11f;
            float padding = 7f;
            float margin = 16f;
            float width = font.getStringWidth(stampText) / 1000f * fontSize + 2 * padding;
            float height = fontSize + 2 * padding;
            if (width + 2 * margin > box.getWidth() || height + 2 * margin > box.getHeight()) {
                throw new IllegalArgumentException("The stamp does not fit on the first page.");
            }

            float left = box.getLowerLeftX();
            float bottom = box.getLowerLeftY();
            float right = box.getUpperRightX();
            float top = box.getUpperRightY();

            float x = switch (position) {
                case TOP_LEFT, BOTTOM_LEFT -> left + margin;
                case TOP_RIGHT, BOTTOM_RIGHT -> right - width - margin;
                case CENTER -> left + (box.getWidth() - width) / 2;
            };
            float y = switch (position) {
                case TOP_LEFT, TOP_RIGHT -> top - height - margin;
                case BOTTOM_LEFT, BOTTOM_RIGHT -> bottom + margin;
                case CENTER -> bottom + (box.getHeight() - height) / 2;
            };

            try (PDPageContentStream stream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                stream.setNonStrokingColor(1f, 1f, 1f);
                stream.addRect(x, y, width, height);
                stream.fill();

                stream.setStrokingColor(48f / 255f, 64f / 255f, 82f / 255f);
                stream.setLineWidth(.6f);
                stream.addRect(x, y, width, height);
                stream.stroke();

                stream.beginText();
                stream.setFont(font, fontSize);
                stream.setNonStrokingColor(20f / 255f, 35f / 255f, 50f / 255f);
                stream.newLineAtOffset(x + padding, y + padding + 2);
                stream.showText(stampText);
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
