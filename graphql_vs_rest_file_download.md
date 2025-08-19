Great question, Sridhar. When you’re storing PDFs as a `byte[]`/`BinData` in MongoDB (or, more appropriately, via GridFS), the **best practice is a hybrid**:

- **Use REST (plain HTTP endpoints) for uploading & downloading the binary stream**  
  (because HTTP handles streaming, content negotiation, `Range` requests, caching, CDNs, and backpressure well).
- **Use GraphQL (or REST) for metadata and search**  
  (because GraphQL shines at querying/filtering typed metadata and relationships).

Below is a practical, architecture-first breakdown.

---

## Why REST for binary & GraphQL for metadata?

### REST (HTTP) strengths for file transfer
- **Native streaming** (no base64 bloat): supports chunked transfer, backpressure, low memory usage.
- **HTTP semantics**: `Content-Type`, `Content-Length`, `ETag`, `Cache-Control`, `Content-Disposition`, `Accept-Ranges: bytes`, and **partial content** (`Range`), which are crucial for PDFs and large files.
- **CDN friendliness**: Edge caching for downloads.
- **Operational simplicity**: Simple clients (browser, mobile, curl) and gateways handle it natively.

### GraphQL strengths for metadata
- Rich querying & filtering for **document metadata** (title, tags, owners, ACLs, status).
- Single round-trip to fetch compound views of metadata and related entities.
- Schema & type safety for clients.

### Why not use GraphQL for file bytes?
- **No native binary streaming**—typical patterns require **base64** encoding (≈33% overhead).
- “Uploads over GraphQL” rely on **non-standard multipart upload specs** and vary by server/client.
- Adds complexity for very large files and operational boundaries (body size limits, memory footprint).

**Conclusion**: Use **REST for bytes** and **GraphQL for brains** (metadata & control).

---

## MongoDB storage choice: Byte array vs GridFS

- **Avoid embedding large PDFs directly as `BinData` in documents**:
  - BSON document limit is **16 MB**.
  - Fetching the doc pulls the entire blob into memory.
  - Indexing and replication overhead.
- **Use MongoDB GridFS** for anything non-trivial:
  - Automatically chunks files (default chunk size 255 KB) into `fs.files` + `fs.chunks`.
  - Supports streaming upload/download via drivers.
  - Store file metadata (MIME, size, SHA-256, owner, tags) in `fs.files.metadata`.
- Alternative (often better): **Object storage** (S3/Azure Blob/GCS) + MongoDB for metadata.  
  But since you asked explicitly about storing in Mongo, GridFS is the right path there.

---

## Recommended API design (Hybrid)

### 1) Binary upload/download (REST)
**Upload (initiate → stream)**
- `POST /documents`  
  **Body**: JSON metadata (e.g., filename, tags, business keys)  
  **Response**: `{ id, uploadUrl, uploadToken }`  
  (*Option A*: return a direct `PUT /documents/{id}/content` URL; *Option B*: resumable protocol.)
- `PUT /documents/{id}/content`  
  **Headers**: `Content-Type: application/pdf`, optional `Content-Range` for resumable.  
  **Body**: raw bytes (stream).  
  **Behavior**: Stream into GridFS using backpressure. Validate MIME+magic bytes.

**Download**
- `GET /documents/{id}/content`  
  **Headers**: respond with `Content-Type: application/pdf`, `Content-Length`, `ETag`, `Accept-Ranges: bytes`, support `Range` requests, and `Content-Disposition` (inline vs attachment).
- `HEAD /documents/{id}/content` for quick metadata & caching checks.

**Delete**
- `DELETE /documents/{id}` soft delete or hard delete file + metadata.

**Versioning (optional)**
- `POST /documents/{id}/versions` → new version id;  
  `GET /documents/{id}/versions` → list & select.

**Security**
- OAuth2/JWT; scope-based (`documents:read`, `documents:write`).
- Server-side antivirus scan (queued, async status).
- Restrict file types (MIME + magic bytes), size limits, rate limits.

### 2) Metadata (GraphQL)
Use GraphQL for listing, filtering, access control views, and issuing **short-lived signed download URLs** (or opaque tokens) that point to REST download endpoints.

```graphql
# Schema sketch
scalar DateTime

type Document {
  id: ID!
  filename: String!
  size: Int!
  mimeType: String!
  sha256: String!
  status: String!       # e.g., "AVAILABLE" | "SCANNING" | "QUARANTINED"
  createdBy: String!
  createdAt: DateTime!
  tags: [String!]!
  version: Int!
}

type Query {
  document(id: ID!): Document
  documents(
    tag: String
    createdBy: String
    from: DateTime
    to: DateTime
    search: String
    limit: Int = 20
    after: String
  ): [Document!]!
  downloadUrl(id: ID!, version: Int): String! # short-lived signed URL/token
}

type Mutation {
  createDocument(filename: String!, tags: [String!]): CreateDocumentPayload!
  updateDocumentMetadata(id: ID!, tags: [String!]): Document!
  deleteDocument(id: ID!): Boolean!
}

type CreateDocumentPayload {
  document: Document!
  uploadUrl: String!    # points to REST PUT endpoint
  uploadToken: String!  # short-lived
}
```

> This keeps file transfer efficient while letting clients enjoy GraphQL’s flexibility for metadata.

---

## Example: Node.js (Express) + MongoDB GridFS (streaming)

> Illustrative only—adjust for your stack. This supports large-file streaming and low memory footprint.

```js
// package.json deps: express, mongodb, mime, helmet
import express from 'express';
import { MongoClient, GridFSBucket, ObjectId } from 'mongodb';
import helmet from 'helmet';
import mime from 'mime';

const app = express();
app.use(helmet());
app.use(express.json({ limit: '1mb' })); // metadata only

const client = await MongoClient.connect(process.env.MONGODB_URI);
const db = client.db(process.env.DB_NAME);
const bucket = new GridFSBucket(db, { bucketName: 'documents' });

// POST /documents -> create metadata & return upload endpoint
app.post('/documents', async (req, res) => {
  const { filename, tags = [] } = req.body || {};
  if (!filename) return res.status(400).json({ error: 'filename required' });

  const mimeType = mime.getType(filename) || 'application/octet-stream';

  // Create a placeholder metadata record in fs.files by writing zero bytes then abort? Better:
  // Generate an id and let the upload create the file entry with metadata on finalize.
  const id = new ObjectId();

  // Issue short-lived token (example: sign JWT or store in cache); here we use a dummy opaque.
  const uploadToken = `${id}.${Date.now()}.${Math.random().toString(36).slice(2, 10)}`;

  // Store a pending record in a side collection for validation/tracking (optional)
  await db.collection('document_meta').insertOne({
    _id: id,
    filename,
    mimeType,
    tags,
    status: 'PENDING_UPLOAD',
    createdAt: new Date(),
  });

  const uploadUrl = `/documents/${id}/content?token=${encodeURIComponent(uploadToken)}`;
  return res.status(201).json({
    id: id.toHexString(),
    uploadUrl,
    uploadToken,
  });
});

// PUT /documents/:id/content -> stream to GridFS
app.put('/documents/:id/content', async (req, res) => {
  const { id } = req.params;
  const token = req.query.token;
  // TODO: validate token and caller authN/Z
  let oid;
  try { oid = new ObjectId(id); } catch { return res.status(400).send('Invalid id'); }

  // Get metadata
  const meta = await db.collection('document_meta').findOne({ _id: oid });
  if (!meta) return res.status(404).send('Unknown document');

  // Validate content-type
  const ct = req.headers['content-type'] || meta.mimeType;
  if (ct !== 'application/pdf') return res.status(415).send('Only application/pdf');

  // Write stream to GridFS; attach metadata
  const uploadStream = bucket.openUploadStreamWithId(oid, meta.filename, {
    contentType: ct,
    metadata: {
      tags: meta.tags,
      createdAt: meta.createdAt,
    },
  });

  req.pipe(uploadStream)
    .on('error', (err) => {
      console.error(err);
      res.status(500).send('Upload failed');
    })
    .on('finish', async () => {
      await db.collection('document_meta').updateOne(
        { _id: oid },
        { $set: { status: 'AVAILABLE', size: uploadStream.length } }
      );
      res.status(201).json({ id, size: uploadStream.length });
    });
});

// GET /documents/:id/content -> stream download, support Range
app.get('/documents/:id/content', async (req, res) => {
  let oid;
  try { oid = new ObjectId(req.params.id); } catch { return res.status(400).send('Invalid id'); }

  // Look up file
  const files = db.collection('documents.files');
  const file = await files.findOne({ _id: oid });
  if (!file) return res.status(404).send('Not found');

  res.setHeader('Content-Type', file.contentType || 'application/pdf');
  res.setHeader('Accept-Ranges', 'bytes');
  res.setHeader('Content-Disposition', `inline; filename="${file.filename}"`);
  // You can set ETag/Cache-Control; compute strong ETag from sha256 if stored.

  // Simplest: full stream (GridFSBucket auto-streams)
  if (!req.headers.range) {
    return bucket.openDownloadStream(oid).pipe(res);
  }

  // Optional: implement byte-range (requires computing start/end)
  // For brevity, omitted detailed Range parsing; many libraries exist for this.
  // Fallback to full stream if Range invalid.
  return bucket.openDownloadStream(oid).pipe(res);
});
```

**Notes**
- Use **backpressure-aware streaming** (`req.pipe(uploadStream)` and `openDownloadStream`).
- Set body size limits only for metadata endpoints, not the file stream route.
- For production, put an API gateway / NGINX in front to handle TLS, `Range`, `ETag`, gzip (not for PDFs), and caching headers.
- For **virus scanning**, push a job on upload completion; expose `status` in metadata (and block download until scanning passes if required).

---

## If you must do *everything* in GraphQL
- Use the **`graphql-multipart-request-spec`** with an `Upload` scalar (e.g., `apollo-server` + `graphql-upload`).  
- Caveats:
  - Reverse proxies and server frameworks often **buffer** the entire body, which defeats streaming for large files.
  - You’ll still need to lift **body size limits** and carefully manage memory.
  - Downloads via GraphQL typically mean **base64** or bespoke endpoints—most teams still fall back to REST for download even in GraphQL-first systems.

Example schema:

```graphql
scalar Upload

type Mutation {
  uploadDocument(file: Upload!, filename: String!, tags: [String!]): Document!
}
```

On server, stream the file upload part into GridFS as soon as the file part comes in. Still consider exposing a REST download endpoint for efficiency.

---

## Operational best practices

- **Hash & deduplicate**: Compute **SHA-256** during upload; if duplicate content detected, reference existing GridFS file to save space.
- **Validation**: Check **magic bytes** (`%PDF-`) and optionally limit page count for safety.
- **Metadata indexes**: `createdAt`, `tags`, `owner`, `status`. If retention applies, use TTL on a logical “expiresAt” field (not on GridFS chunks).
- **Access control**: Per-document ACL in metadata; enforce on both metadata API and binary endpoints.
- **Observability**: Log `documentId`, bytes uploaded/downloaded, duration, user, and outcome. Emit metrics (p95 latency, error rates).
- **Backups**: Ensure both `fs.files` and `fs.chunks` collections are backed up consistently.
- **Large scale**: Consider separating **upload** and **download** services, with download behind a CDN. If using Atlas, place app servers close to the cluster.

---

## Decision checklist

- **Files > 10–20 MB or need partial download?** → REST streaming + GridFS.
- **Rich querying/filtering of metadata across entities?** → GraphQL for metadata.
- **Mobile/browser clients with flaky networks?** → Add resumable uploads (e.g., `Content-Range` or Tus).
- **CDN offload for downloads?** → REST with cacheable URLs and proper headers.

---

## Recommendation

Given your context and likely enterprise scale:

1. **Use GridFS** to store PDFs in MongoDB (avoid embedding large `BinData` in documents).
2. **Expose REST endpoints for upload/download** with streaming, `Range`, and caching headers.
3. **Expose GraphQL for metadata** (create/list/search, issue short-lived signed download/upload URLs).
4. Add **antivirus scanning**, **SHA-256 dedupe**, and **fine-grained authorization**.

This gives you operational efficiency for binaries and developer productivity for metadata.

---
