# Export Module - Quick Start Guide

## Overview

The export module provides:
1. **Document Retrieval** from MinIO storage
2. **Document Export** in multiple formats (PDF, HTML)
3. **Content Management** with proper HTTP headers

## Quick Start

### 1. Verify MinIO Configuration

Check your `application.yml`:
```yaml
minio:
  url: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucket: copilot-documents
```

### 2. Key Endpoints

#### Get Document Content (MinIO)
```bash
GET /api/documents/{documentId}/content
```
**Response:** Document bytes with `Content-Type` and `Content-Disposition` headers

**Example:**
```bash
curl http://localhost:8080/api/documents/1/content -o myfile.pdf
```

**Response Headers:**
```
Content-Type: application/pdf
Content-Disposition: inline; filename="document.pdf"
```

#### Export Document to Format
```bash
POST /api/documents/{documentId}/export?format={PDF|HTML}
Content-Type: application/json

{
  "title": "Document Title",
  "content": "Document content...",
  "author": "Author Name",
  "description": "Document description"
}
```

**Response:**
```json
{
  "downloadUrl": "http://localhost:8080/api/exports/download/uuid/filename.pdf",
  "fileName": "Document_Title_12345.pdf",
  "contentType": "application/pdf"
}
```

**Example:**
```bash
curl -X POST "http://localhost:8080/api/documents/1/export?format=PDF" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Sales Report",
    "content": "Annual sales data for 2024",
    "author": "John Doe",
    "description": "PDF export of sales report"
  }'
```

#### List Supported Formats
```bash
GET /api/documents/export/formats
```

**Response:**
```json
["PDF", "DOCX", "HTML"]
```

## How Document Retrieval Works

```
User Request: GET /api/documents/1/content
    ↓
ExportController.showDocument(1)
    ↓
DocumentRepository.findById(1)
    → Returns: Document{fileName, contentType, objectKey}
    ↓
MinioDocumentService.getDocument(objectKey)
    ↓
MinioClient.getObject(bucket, objectKey)
    ↓
Returns: byte[] with Content-Type header
```

## Error Handling

| Error | Status | Solution |
|-------|--------|----------|
| Document not found | 404 | Check document ID exists in database |
| MinIO unavailable | 500 | Verify MinIO is running and accessible |
| Invalid format | 400 | Use PDF, DOCX, or HTML |

**Example Error Response:**
```bash
curl http://localhost:8080/api/documents/999/content
# Returns: 404 NOT_FOUND
```

## Troubleshooting

### Document Not Found (404)
```bash
# Verify document exists
curl http://localhost:8080/api/documents

# Check specific document
SELECT * FROM documents WHERE id = 1;
```

### MinIO Connection Error (500)
```bash
# Verify MinIO is running
docker ps | grep minio

# Check MinIO logs
docker logs <minio-container>

# Verify configuration
curl http://localhost:9000
```

### Content Type Issues
The module automatically sets content type from the document entity:
```java
.header("Content-Type", document.getContentType())
```

If content type is wrong, update the document entity or adjust accordingly.

## Logging

Monitor operations via logs:

```bash
# Tail logs for document retrieval
tail -f logs/application.log | grep "Retrieving document"

# Check for MinIO errors
tail -f logs/application.log | grep "MinIO"

# View all export operations
tail -f logs/application.log | grep "export"
```

**Log Examples:**
```
INFO  - Retrieving document content for ID: 1
DEBUG - Document found: 1 with objectKey: uploads/doc-12345.pdf
DEBUG - Fetching object from MinIO: bucket=copilot-documents, objectKey=uploads/doc-12345.pdf
INFO  - Successfully retrieved document 1 from MinIO (size: 245632 bytes)
```

## Code Examples

### Retrieve Document in Java
```java
@Autowired
private RestTemplate restTemplate;

public void downloadDocument(Long documentId) {
    byte[] documentContent = restTemplate.getForObject(
        "http://localhost:8080/api/documents/{id}/content",
        byte[].class,
        documentId
    );
    // Save or process documentContent
}
```

### Export Document in Java
```java
@Autowired
private RestTemplate restTemplate;

public ExportResponse exportDocument(Long documentId) {
    ExportRequest request = new ExportRequest(
        "My Title",
        "Content here",
        "Author",
        "Description"
    );
    
    ExportResponse response = restTemplate.postForObject(
        "http://localhost:8080/api/documents/{id}/export?format=PDF",
        request,
        ExportResponse.class,
        documentId
    );
    return response;
}
```

### JavaScript/Fetch API
```javascript
// Retrieve document
fetch(`/api/documents/1/content`)
  .then(response => response.blob())
  .then(blob => {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'document.pdf';
    a.click();
  });

// Export document
const exportRequest = {
  title: "Sales Report",
  content: "2024 sales data",
  author: "John Doe",
  description: "Annual report"
};

fetch(`/api/documents/1/export?format=PDF`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(exportRequest)
})
  .then(response => response.json())
  .then(data => console.log('Export URL:', data.downloadUrl));
```

## Next Steps

1. **Test the endpoints** using curl or Postman
2. **Integrate with UI** - Add download/export buttons
3. **Add DOCX export** - Implement Apache POI integration
4. **Implement async export** - For large documents
5. **Add authorization** - Verify user access rights

## Support

For issues or questions:
1. Check logs in `logs/application.log`
2. Verify MinIO connectivity
3. Ensure database has document records
4. Check document `objectKey` matches MinIO objects

## Files Reference

- **Controller:** [ExportController.java](export/controller/ExportController.java)
- **Service:** [ExportService.java](export/service/ExportService.java)
- **MinIO Service:** [MinioDocumentService.java](export/service/MinioDocumentService.java)
- **Documentation:** [README.md](export/README.md)

---

**Last Updated:** 2026-07-06
