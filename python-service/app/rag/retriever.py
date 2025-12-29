import chromadb
from chromadb.config import Settings as ChromaSettings
from app.config import get_settings
from app.rag.embeddings import get_embedding_service
from typing import List, Dict

settings = get_settings()

class VectorRetriever:
    def __init__(self):
        self.client = chromadb.PersistentClient(
            path=settings.chroma_persist_dir,
            settings=ChromaSettings(anonymized_telemetry=False)
        )
        self.collection = self.client.get_or_create_collection(
            name="tea_reviews",
            metadata={"description": "Tea shop reviews and blog posts"}
        )
        self.embedding_service = get_embedding_service()
    
    def add_document(self, doc_id: str, text: str, metadata: Dict):
        """Add a single document to the vector store"""
        embedding = self.embedding_service.embed_query(text)
        self.collection.add(
            ids=[doc_id],
            embeddings=[embedding],
            documents=[text],
            metadatas=[metadata]
        )
    
    def add_documents_batch(self, doc_ids: List[str], texts: List[str], metadatas: List[Dict]):
        """Add multiple documents at once"""
        embeddings = self.embedding_service.embed_documents(texts)
        self.collection.add(
            ids=doc_ids,
            embeddings=embeddings,
            documents=texts,
            metadatas=metadatas
        )
    
    def search(self, query: str, n_results: int = 5) -> Dict:
        """Search for similar documents"""
        query_embedding = self.embedding_service.embed_query(query)
        results = self.collection.query(
            query_embeddings=[query_embedding],
            n_results=n_results
        )
        return results
    
    def get_count(self) -> int:
        """Get total number of documents"""
        return self.collection.count()

# Singleton instance
_retriever = None

def get_retriever() -> VectorRetriever:
    global _retriever
    if _retriever is None:
        _retriever = VectorRetriever()
    return _retriever