Core Mathematical Difference
| Metric | Mathematical Formula | Range | Normalization Required? | What It Measures |
|---|---|---|---|---|
| Cosine Similarity | \cos(\theta) = \frac{\mathbf{u} \cdot \mathbf{v}}{\Vert{}\mathbf{u}\Vert{}\Vert{}\mathbf{v}\Vert{}} | [-1.0, 1.0] | No (normalizes on the fly) | Direction / Angle only (completely ignores magnitude). |
| Inner Product (Dot Product) | \mathbf{u} \cdot \mathbf{v} = \sum_{i=1}^n u_i v_i | (-\infty, \infty) | No (computes raw product) | Direction AND Magnitude combined. |
 * Cosine Similarity evaluates only the cosine of the angle \theta between two vectors. It divides the dot product by the product of both vector lengths (\Vert{}\mathbf{u}\Vert{} and \Vert{}\mathbf{v}\Vert{}), meaning vectors pointing in the exact same direction will always yield a score of 1.0, regardless of how long or short they are.
 * Inner Product calculates the unnormalized scalar sum of products across all dimensions. Longer vectors (larger magnitudes) naturally yield higher inner product scores even if their directional alignment is slightly off.
The Mathematical Equivalence (Pre-Normalized Vectors)
When vectors are pre-normalized to unit length (\Vert{}\mathbf{u}\Vert{} = 1 and \Vert{}\mathbf{v}\Vert{} = 1), the denominator in cosine similarity becomes 1 \times 1 = 1:
Under this condition, Cosine Similarity and Inner Product produce identical relative rankings.
Computational Advantage in Vector Databases (OpenSearch & pgvector)
In large-scale approximate nearest neighbor (ANN) searches (such as HNSW graph traversals), distance calculations are executed thousands of times per query:
 * Cosine Distance (cosinesimil / <=>): The engine must calculate square roots, compute vector magnitudes, and perform division on every single edge evaluation to normalize vectors on the fly.
 * Inner Product (innerproduct / <#>): Skips the magnitude and division steps entirely, performing only simple fused multiply-add operations (dot product).
> Production Impact: Using Inner Product over pre-normalized vectors produces the exact same search ranking as Cosine Similarity while significantly reducing CPU cycle overhead, lowering query latency, and improving throughput.
> 
When to Choose Which
1. Choose innerproduct
 * Pre-normalized embeddings: Modern foundation models such as Amazon Titan Text Embeddings V2 (when setting "normalize": true in Bedrock) or OpenAI text-embedding-3 generate unit-length vectors. Using innerproduct in OpenSearch or <#> (vector_ip_ops) in pgvector delivers the lowest query latency and CPU overhead.
 * Maximum Inner Product Search (MIPS): Recommendation systems where vector magnitude explicitly denotes importance, popularity, or item frequency that should bias the ranking.
2. Choose cosinesimil
 * Unnormalized text models: Legacy sentence transformers or embedding models that do not output unit vectors.
 * Variable chunk sizes: When document chunk lengths vary widely and larger chunks artificially inflate vector magnitude, cosine similarity prevents longer chunks from dominating the nearest-neighbor results.
