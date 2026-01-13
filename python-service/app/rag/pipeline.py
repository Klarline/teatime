from app.rag.retriever import get_retriever
from app.rag.generator import get_generator
from typing import Tuple, List

class RAGPipeline:
    def __init__(self):
        self.retriever = get_retriever()
        self.generator = get_generator()
    
    def recommend(self, query: str, max_results: int = 5) -> Tuple[str, List[int]]:
        """
        End-to-end recommendation pipeline
        Returns: (recommendation_text, list_of_source_blog_ids)
        """
        # Retrieve relevant documents
        search_results = self.retriever.search(query, n_results=max_results)
        
        if not search_results['documents'][0]:
            return "I don't have enough information about tea shops yet. Please check back after some reviews are added!", []
        
        # Extract documents and metadata
        documents = search_results['documents'][0]
        metadatas = search_results['metadatas'][0]
        
        # Generate recommendation
        recommendation = self.generator.generate_recommendation(query, documents, metadatas)
        
        # Extract source blog IDs
        source_blog_ids = [meta['blog_id'] for meta in metadatas]
        
        return recommendation, source_blog_ids

# Singleton instance
_pipeline = None

def get_pipeline() -> RAGPipeline:
    global _pipeline
    if _pipeline is None:
        _pipeline = RAGPipeline()
    return _pipeline