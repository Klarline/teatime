from sentence_transformers import SentenceTransformer
from app.config import get_settings

settings = get_settings()

class EmbeddingService:
    def __init__(self):
        # Use local model instead of Google API
        print("Loading local embedding model...")
        self.model = SentenceTransformer('all-MiniLM-L6-v2')
        print("Embedding model loaded!")
    
    def embed_query(self, text: str) -> list[float]:
        """Generate embedding for a query"""
        embedding = self.model.encode(text, convert_to_tensor=False)
        return embedding.tolist()
    
    def embed_documents(self, texts: list[str]) -> list[list[float]]:
        """Generate embeddings for multiple documents"""
        embeddings = self.model.encode(texts, convert_to_tensor=False)
        return [emb.tolist() for emb in embeddings]

# Singleton instance
_embedding_service = None

def get_embedding_service() -> EmbeddingService:
    global _embedding_service
    if _embedding_service is None:
        _embedding_service = EmbeddingService()
    return _embedding_service