# PDF Stamp Creation

A standalone Spring Boot and Apache PDFBox application for adding a customizable text stamp to a PDF.

## Features
- Upload a PDF in the browser
- Enter custom stamp text (default: `APPROVED – 12345`)
- Choose a position on **the first page**: top right, top left, bottom right, bottom left or centre
- Preview and download the stamped PDF
- Try the included `samples/sample.pdf`
- The application does not intentionally store uploaded PDFs

## Requirements
- Java 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

Open http://localhost:8080.

## API

`POST /api/stamp` with `multipart/form-data`:

| Field | Description |
|---|---|
| `file` | PDF file |
| `stampText` | Text to place on the stamp |
| `position` | `TOP_RIGHT` (default), `TOP_LEFT`, `BOTTOM_RIGHT`, `BOTTOM_LEFT`, `CENTER` |

Returns the stamped PDF. Maximum upload size is 20 MB.

## Limitations

This is a local technical demonstration, not a production-grade document processing service. Rotated pages and encrypted PDFs are not supported; the stamp is added to page 1 only. Built-in Helvetica supports limited characters. Stamping a signed PDF invalidates its existing signature. Review the preview before downloading, because a stamp may obscure existing text.
