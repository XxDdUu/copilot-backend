# Export Module

This module provides document export functionality supporting multiple formats (PDF, DOCX, HTML).

## Architecture

```
export/
├── controller/
│   └── ExportController.java      # REST endpoints for export operations
├── service/
│   ├── ExportService.java         # Main export service orchestrator
│   └── PdfExporter.java           # PDF format handler
├── dto/
│   ├── ExportResponse.java        # Export response DTO
│   └── ExportRequest.java         # Export request DTO
└── format/
    └── ExportFormat.java          # Supported export formats enum
```

## Features

- **Multiple Export Formats**: PDF, DOCX, HTML
- **Document Metadata**: Title, Author, Description
- **File Management**: Automatic file naming and storage
- **Download URLs**: Generate shareable download links
- **Error Handling**: Comprehensive exception handling and logging

## API Endpoints

### Export Document
```
POST /api/documents/{documentId}/export?format={format}
Content-Type: application/json

# Export Module

This module provides document export functionality supporting multiple formats (PDF, DOCX, HTML).
{
  "title": "Document Title",
  "content": "Document content...",
  "author": "Author Name",
  "description": "Document description"
}

Response:
{
  "downloadUrl": "http://localhost:8080/api/exports/download/uuid/filename.pdf",
  "fileName": "Document_Title_12345.pdf",
  "contentType": "application/pdf"
}
```

### Get Document Content
```
GET /api/documents/{id}/content

Response: Document content string
```

### List Supported Formats
```
- **MinIO Integration**: Seamless retrieval of documents from MinIO storage
- **Content Type Support**: Automatic content-type headers
- **Stream Handling**: Efficient byte stream handling from MinIO
GET /api/documents/export/formats

Response: ["PDF", "DOCX", "HTML"]
```

## Supported Export Formats

| Format | Content-Type | Extension | Status |
|--------|--------------|-----------|--------|
| PDF | application/pdf | .pdf | ✓ Ready |
| DOCX | application/vnd.openxmlformats-officedocument.wordprocessingml.document | .docx | ⚠️ TODO |
| HTML | text/html | .html | ✓ Ready |

## Dependencies

### Required (in pom.xml or build.gradle)
```xml
<!-- For PDF export with iText 7 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
</dependency>

<!-- For DOCX export with Apache POI -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.1.0</version>
**Features:**
- Retrieves document metadata from database
- Fetches document content from MinIO storage
- Returns document with proper `Content-Type` and `Content-Disposition` headers
- Supports streaming for large files
</dependency>
**Response Headers:**
```
Content-Type: application/pdf  (or appropriate MIME type)
Content-Disposition: inline; filename="document.pdf"
```
```
**Error Responses:**
- `404 NOT_FOUND`: Document not found in database
- `500 INTERNAL_SERVER_ERROR`: MinIO retrieval failed

### Example Usage
```bash
# Retrieve a PDF document
curl -i http://localhost:8080/api/documents/1/content

# Retrieve with specific format
curl -i http://localhost:8080/api/documents/1/content \
  -H "Accept: application/pdf"
```
## Configuration

Add to `application.properties` or `application.yml`:

```properties
# Export Configuration
export.storage.path=./exports
export.download.url=http://localhost:8080/api/exports/download
export.max.file.size=52428800
export.temp.directory=./temp/exports
```

## Usage Examples

### Export to PDF
```bash
curl -X POST http://localhost:8080/api/documents/1/export?format=PDF \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Document",
    "content": "This is the document content",
    "author": "John Doe",
    "description": "Sample document for export"
  }'
```

### Export to HTML
```bash
curl -X POST http://localhost:8080/api/documents/1/export?format=HTML \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Document",
    "content": "This is the document content",
    "author": "John Doe",
    "description": "Sample document for export"
  }'
```

### Get Document Content
```bash
curl http://localhost:8080/api/documents/1/content
```

## TODO

- [ ] Implement DOCX export using Apache POI
- [ ] Add iText 7 integration for advanced PDF features
- [ ] Implement file download endpoint (`GET /api/exports/download/{fileId}/{fileName}`)
- [ ] Add export history tracking
- [ ] Implement export templates
- [ ] Add batch export functionality
- [ ] Add export scheduling/async support
- [ ] Implement export encryption for sensitive documents
- [ ] Add export audit logging
- [ ] Performance optimization for large documents

## Future Enhancements

1. **Async Export**: Support background export processing
2. **Export Templates**: Pre-defined export configurations
3. **Batch Export**: Export multiple documents at once
4. **Export History**: Track exported documents
5. **Custom Branding**: Add logo and styling to exports
6. **Export Encryption**: Password-protected exports
7. **Digital Signatures**: Sign exported PDFs
8. **Compression**: Archive exports as ZIP
