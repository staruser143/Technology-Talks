# Cross Encoder Model: Cosine Similarity vs. Inner Product

## Core mathematical difference

| Metric | Mathematical Formula | Range | Normalization Required? | What It Measures |
|---|---|---:|---|---|
| Cosine Similarity | $\cos(\theta) = \frac{\mathbf{u} \cdot \mathbf{v}}{\Vert\mathbf{u}\Vert\Vert\mathbf{v}\Vert}$ | [-1.0, 1.0] | No (normalizes on the fly) | Direction / angle only; ignores magnitude |
| Inner Product (Dot Product) | $\mathbf{u} \cdot \mathbf{v} = \sum_{i=1}^n u_i v_i$ | $(-\infty, \infty)$ | No (computes raw product) | Direction and magnitude together |

### Why they differ

- Cosine similarity evaluates only the angle between two vectors. It divides the dot product by the product of both vector lengths, so two vectors pointing in the same direction will always score 1.0, regardless of their absolute length.
- Inner product calculates the raw sum of element-wise products across all dimensions. As a result, longer vectors tend to produce larger scores, even when their alignment is only slightly better.

## Mathematical equivalence for normalized vectors

When vectors are pre-normalized to unit length, $\Vert\mathbf{u}\Vert = 1$ and $\Vert\mathbf{v}\Vert = 1$, the denominator in cosine similarity becomes:

$1 \times 1 = 1$

So cosine similarity reduces to the inner product:

$\cos(\theta) = \mathbf{u} \cdot \mathbf{v}$

Under this condition, both metrics produce the same relative ranking.

## Computational advantage in vector databases

In large-scale approximate nearest-neighbor (ANN) search, distance calculations may be run thousands of times per query.

- Cosine distance (`cosinesimil` / `<=>`): requires computing vector magnitudes and division for each evaluation.
- Inner product (`innerproduct` / `<#>`): skips the magnitude and division steps and performs only simple fused multiply-add operations.

> Production impact: When vectors are pre-normalized, using inner product yields the same ranking as cosine similarity while lowering CPU cost, reducing latency, and improving throughput.

## When to choose each metric

### Choose `innerproduct` when

- Embeddings are already normalized. Modern models such as Amazon Titan Text Embeddings V2 with `"normalize": true` and OpenAI `text-embedding-3` commonly produce unit-length vectors.
- You are using OpenSearch or `pgvector` and want the lowest-latency ANN search.
- You are doing maximum inner product search (MIPS), where vector magnitude is meaningful and should influence ranking.

### Choose `cosinesimil` when

- The embedding model does not output normalized vectors.
- Chunk sizes vary significantly and longer chunks would otherwise dominate due to higher magnitude.
- You want a distance metric that ignores vector length and focuses on semantic direction.

## Recommendation

If your vectors are normalized, prefer `innerproduct`. It is mathematically equivalent to cosine similarity for ranking and is more efficient in production vector databases. Use `cosinesimil` when magnitude should not influence similarity or when working with unnormalized embedding outputs.
