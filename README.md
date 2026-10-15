# PDF Stamp Creation — SOLSTISS / GESDEC prototype

A standalone Spring Boot + Apache PDFBox application for testing PDF stamps.

## Features
- Upload a PDF locally in your browser
- Enter any stamp text (default: `GESDEC – SONDAGE-001`)
- Apply the stamp to the **first page, top right**
- Preview the stamped PDF in-browser
- Download the stamped PDF
- A sample PDF is included in `samples/sample.pdf`
- No database, GED, login or permanent file storage

## Requirements
- JDK 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

Open **http://localhost:8080** and test with `samples/sample.pdf` or another PDF.

## API

`POST /api/stamp` as `multipart/form-data`:
- `file`: source PDF
- `stampText`: string (for example `GESDEC – SONDAGE-001`)

Returns `application/pdf` stamped PDF as an attachment. Uploaded PDFs are processed in memory and are not intentionally persisted.

## Scope and limitations
This is a **demonstrator**, not production-ready SOLSTISS code. For production, validate the selected sondage against project permissions and look up the sondage number server-side. Harden handling of encrypted/malformed or very large PDFs, rotated pages, PDF/A constraints, existing signatures, clipping and overlapping text. Stamping modifies PDF bytes and will invalidate existing digital signatures.

Stamp style: small white backing box with black text at top right of page 1. The original PDF is not stored by this demo. Maximum upload 20MB.

## License
Example code for technical evaluation.
