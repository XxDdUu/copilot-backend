# Export Module Architecture Diagram

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Application                        │
│  (Web Browser / Mobile App / REST Client)                       │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ HTTP Request
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              ExportController                            │  │
│  │  ├─ POST /{documentId}/export?format={format}          │  │
│  │  ├─ GET /{id}/content                  ◄── YOUR REQUEST│  │
│  │  └─ GET /export/formats                                 │  │
│  └──────────────────┬───────────────────────────────────────┘  │
│                     │                                            │
│  ┌──────────────────▼───────────────────────────────────────┐  │
│  │           Repository Layer                              │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │ DocumentRepository.findById(id)                    │ │  │
│  │  │ Returns: Document{id, fileName, contentType,     │ │  │
│  │  │                   objectKey, uploadedBy, ...}    │ │  │
│  │  └────────────────────────────────────────────────────┘ │  │
│  └──────────────────┬───────────────────────────────────────┘  │
│                     │                                            │
│  ┌──────────────────▼───────────────────────────────────────┐  │
│  │           Service Layer                                 │  │
│  │  ┌─────────────────────────────────────────────────┐   │  │
│  │  │ MinioDocumentService.getDocument(objectKey)   │   │  │
│  │  │  └─ Returns: byte[] (document content)        │   │  │
│  │  └─────────────────────────────────────────────────┘   │  │
│  │  ┌─────────────────────────────────────────────────┐   │  │
│  │  │ ExportService (for export operations)          │   │  │
│  │  │  ├─ exportToHtml()                             │   │  │
│  │  │  ├─ exportToPdf()                              │   │  │
│  │  │  └─ exportToDocx()                             │   │  │
│  │  └─────────────────────────────────────────────────┘   │  │
│  │  ┌─────────────────────────────────────────────────┐   │  │
│  │  │ PdfExporter                                    │   │  │
│  │  │  └─ exportToPdf(title, content, author, ...)  │   │  │
│  │  └─────────────────────────────────────────────────┘   │  │
│  └──────────────────┬───────────────────────────────────────┘  │
│                     │                                            │
│  ┌──────────────────▼───────────────────────────────────────┐  │
│  │           Client/Integration Layer                      │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │ MinioClient                                        │ │  │
│  │  │  ├─ getObject(bucket, objectKey) ◄─── MinIO API  │ │  │
│  │  │  ├─ putObject(bucket, objectKey, stream)         │ │  │
│  │  │  └─ bucketExists(bucket)                         │ │  │
│  │  └────────────────────────────────────────────────────┘ │  │
│  └──────────────────┬───────────────────────────────────────┘  │
│                     │                                            │
└─────────────────────┼────────────────────────────────────────────┘
                      │
                      │ gRPC / S3-compatible API
                      ▼
        ┌──────────────────────────────────┐
        │    MinIO Storage Service         │
        │                                  │
        │  Bucket: copilot-documents      │
        │  ├─ uploads/                    │
        │  │  ├─ doc-uuid-1.pdf          │
        │  │  ├─ doc-uuid-2.docx         │
        │  │  └─ ...                      │
        │  └─ exports/                   │
        │     ├─ export-id-1.pdf        │
        │     └─ ...                     │
        │                                │
        └──────────────────────────────────┘
```

## Data Flow Diagram - GET /api/documents/{id}/content

```
┌─────────────────────────────────────────────────────────────────┐
│ User Request                                                    │
│ GET /api/documents/1/content                                   │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 1. ExportController.showDocument(id: 1)                        │
│    ├─ Log: "Retrieving document content for ID: 1"            │
│    └─ documentRepository.findById(1)                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. Query Database (JPA)                                        │
│    SELECT * FROM documents WHERE id = 1                       │
│                                                                │
│    Returns: Document {                                         │
│      id: 1,                                                   │
│      fileName: "report.pdf",                                 │
│      contentType: "application/pdf",                         │
│      objectKey: "uploads/doc-uuid-1.pdf",                   │
│      uploadedBy: User{...},                                 │
│      createdAt: 2024-01-15 10:30:45                        │
│    }                                                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. Validate Document Found                                     │
│    ├─ If NOT found → throw IllegalArgumentException           │
│    │  └─ Return: 404 NOT_FOUND                               │
│    │                                                          │
│    └─ If found:                                              │
│       ├─ Log: "Document found: 1 with objectKey: ..."      │
│       └─ Continue                                           │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. Retrieve from MinIO                                         │
│    retrieveDocumentFromMinIO(objectKey: "uploads/doc-uuid-1")│
│    └─ MinioDocumentService.getDocument(objectKey)             │
│       └─ MinioClient.getObject(                              │
│          bucket: "copilot-documents",                        │
│          object: "uploads/doc-uuid-1.pdf"                   │
│       )                                                       │
│       └─ InputStream.readAllBytes() → byte[]                │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. Build HTTP Response                                         │
│    ResponseEntity.ok()                                         │
│      .header("Content-Type", "application/pdf")              │
│      .header("Content-Disposition",                          │
│             "inline; filename=\"report.pdf\"")              │
│      .body(byte[])                                           │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. HTTP Response Sent to Client                               │
│    HTTP/1.1 200 OK                                            │
│    Content-Type: application/pdf                             │
│    Content-Disposition: inline; filename="report.pdf"       │
│    Content-Length: 245632                                   │
│    [Binary PDF Data: 245632 bytes]                          │
└─────────────────────────────────────────────────────────────────┘
```

## Export Process Flow - POST /api/documents/{id}/export?format=PDF

```
┌─────────────────────────────────────────────────────────────────┐
│ User Request                                                    │
│ POST /api/documents/1/export?format=PDF                        │
│ {                                                              │
│   "title": "Annual Report",                                   │
│   "content": "Sales data for 2024...",                       │
│   "author": "John Doe",                                      │
│   "description": "Q4 2024 Report"                           │
│ }                                                              │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ ExportController.export(documentId: 1, format: PDF, request)   │
│ ├─ Log: "Exporting document 1 to format PDF"                 │
│ └─ ExportService.exportDocument(1, PDF, request)             │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ ExportService.exportDocument()                                 │
│ ├─ Switch on format (PDF)                                    │
│ ├─ PdfExporter.exportToPdf(                                  │
│ │  title: "Annual Report",                                 │
│ │  content: "Sales data...",                              │
│ │  author: "John Doe",                                    │
│ │  description: "Q4 2024 Report"                         │
│ │)                                                        │
│ ├─ Returns: byte[] (PDF content)                          │
│ └─ fileName = "Annual_Report_1705314645000.pdf"          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ Save Exported File                                             │
│ ├─ fileId = UUID.randomUUID()                               │
│ ├─ MinioDocumentService.uploadDocument(                     │
│ │  fileId,                                                │
│ │  byte[],                                               │
│ │  "application/pdf"                                    │
│ │)                                                       │
│ └─ Stored in: exports/{fileId}/                         │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ Generate Response                                              │
│ {                                                              │
│   "downloadUrl": "http://.../{fileId}/Annual_Report_...pdf",│
│   "fileName": "Annual_Report_1705314645000.pdf",            │
│   "contentType": "application/pdf"                          │
│ }                                                              │
└─────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ HTTP 200 OK Response to Client                               │
│ {                                                              │
│   "downloadUrl": "...",                                      │
│   "fileName": "...",                                         │
│   "contentType": "application/pdf"                          │
│ }                                                              │
└─────────────────────────────────────────────────────────────────┘
```

## Component Interaction Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    ExportController                             │
│  • export()                                                      │
│  • showDocument() ◄─── YOUR IMPLEMENTATION                      │
│  • listExportFormats()                                           │
└────────────┬────────────────────────────┬──────────────────────┘
             │                            │
             ▼                            ▼
    ┌──────────────────┐      ┌──────────────────────┐
    │  ExportService   │      │ MinioDocumentService │
    │                  │      │                      │
    │ • exportDocument │      │ • getDocument()      │
    │ • exportToHtml() │      │ • uploadDocument()   │
    │ • exportToPdf()  │      │ • documentExists()   │
    │ • exportToDocx() │      │ • getDocumentSize()  │
    └──────────────────┘      └──────────┬───────────┘
             │                           │
             │                           ▼
             │                   ┌──────────────────┐
             │                   │  MinioClient     │
             │                   │                  │
             │                   │ • getObject()    │
             │                   │ • putObject()    │
             │                   │ • bucketExists() │
             │                   └────────┬─────────┘
             │                           │
             ├───────────┬───────────────┤
             │           │               │
             ▼           ▼               ▼
    ┌─────────────┐ ┌──────────┐ ┌────────────────┐
    │ PdfExporter │ │Database  │ │ MinIO Storage  │
    │             │ │          │ │                │
    │ • export    │ │Documents │ │ Buckets        │
    │   ToPdf()   │ │ table    │ │ Objects        │
    └─────────────┘ └──────────┘ └────────────────┘
```

## Error Handling Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ GET /api/documents/{id}/content                               │
└────────────────────────┬────────────────────────────────────────┘
                         │
                    Try / Catch
                         │
                ┌────────┴────────┐
                │                 │
                ▼                 ▼
        ┌───────────────┐  ┌──────────────────┐
        │ Happy Path    │  │ Exception Path   │
        │               │  │                  │
        │ • findById OK │  │ • Document not   │
        │ • MinIO OK    │  │   found → 404    │
        │ • Read OK     │  │                  │
        │ • Return 200  │  │ • MinIO error →  │
        │   with bytes  │  │   500            │
        └───────────────┘  │                  │
                           │ • Other error →  │
                           │   500            │
                           └──────────────────┘
```

## Database Schema

```
┌─────────────────────────────────────────────────────────────────┐
│                      documents                                   │
├─────────────────────────────────────────────────────────────────┤
│ id (PK)                  │ BIGINT                              │
│ file_name                │ VARCHAR(255)                        │
│ content_type             │ VARCHAR(100)                        │
│ object_key               │ VARCHAR(500)  ← MinIO path         │
│ uploaded_by (FK)         │ BIGINT → users.id                  │
│ created_at               │ TIMESTAMP                          │
│ [other fields...]        │ ...                                │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      users                                       │
├─────────────────────────────────────────────────────────────────┤
│ id (PK)                  │ BIGINT                              │
│ email                    │ VARCHAR(255)                        │
│ [other fields...]        │ ...                                │
└─────────────────────────────────────────────────────────────────┘
```

## MinIO Storage Structure

```
copilot-documents/ (Bucket)
├── uploads/
│   ├── doc-uuid-1.pdf
│   ├── doc-uuid-2.docx
│   ├── doc-uuid-3.txt
│   └── ...
├── exports/
│   ├── export-id-1.pdf
│   ├── export-id-2.html
│   └── ...
└── temp/
    └── [temporary files]
```

---

**Created:** 2026-07-06  
**Status:** Complete
