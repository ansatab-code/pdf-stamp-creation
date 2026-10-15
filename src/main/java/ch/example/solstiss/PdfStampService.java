package ch.example.solstiss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

@Service
public class PdfStampService {
    public byte[] stamp(byte[] sourcePdf, String requestedText) throws IOException {
        if (requestedText == null || requestedText.isBlank() || requestedText.length() > 100) {
            throw new IllegalArgumentException("Stamp text must contain 1–100 characters.");
        }
        // Standard-14 Helvetica uses a limited encoding. Keep the demo predictable
        // for text entered using arbitrary Unicode characters.
        String stampText = requestedText.trim();
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        try {
            font.encode(stampText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Stamp contains unsupported characters. Use Latin text and standard punctuation.");
        }

        try (PDDocument document = Loader.loadPDF(sourcePdf);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (document.getNumberOfPages() < 1) {
                throw new IllegalArgumentException("The PDF has no pages.");
            }
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("Encrypted PDFs are not supported by this demo.");
            }
            PDPage page = document.getPage(0);
            PDRectangle box = page.getCropBox();
            float fontSize = 11f;
            float padding = 7f;
            float margin = 16f;
            float width = font.getStringWidth(stampText) / 1000f * fontSize + 2 * padding;
            float height = fontSize + 2 * padding;
            if (width + 2 * margin > box.getWidth()) {
                throw new IllegalArgumentException("Stamp is too long for the first page.");
            }

            // This proof of concept targets unrotated pages. Reject rotated pages
            // rather than placing the stamp in the wrong visual corner.
            if (page.getRotation() % 360 != 0) {
                throw new IllegalArgumentException("Rotated PDF pages are not supported in this prototype.");
            }
            float x = box.getUpperRightX() - width - margin;
            float y = box.getUpperRightY() - height - margin;

            try (PDPageContentStream stream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                stream.setNonStrokingColor(255, 255, 255);
                stream.addRect(x, y, width, height);
                stream.fill();

                stream.setStrokingColor(48, 64, 82);
                stream.setLineWidth(.6f);
                stream.addRect(x, y, width, height);
                stream.stroke();

                stream.beginText();
                stream.setFont(font, fontSize);
                stream.setNonStrokingColor(20, 35, 50);
                stream.newLineAtOffset(x + padding, y + padding + 2);
                stream.showText(stampText);
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
