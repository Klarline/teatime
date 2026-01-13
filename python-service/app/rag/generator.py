import google.generativeai as genai
from app.config import get_settings

settings = get_settings()
genai.configure(api_key=settings.google_api_key)

class LLMGenerator:
    def __init__(self):
        self.model = genai.GenerativeModel(settings.gemini_model)
    
    def generate_recommendation(self, query: str, context_documents: list[str], metadatas: list[dict]) -> str:
        """Generate recommendation based on query and retrieved documents"""
        
        # Build context from retrieved documents WITH SHOP NAMES
        context = "\n\n".join([
            f"Shop Name: {meta['shop_name']}\n{doc}" 
            for doc, meta in zip(context_documents, metadatas)
        ])
        
        prompt = f"""You are a helpful tea shop recommendation assistant. Based STRICTLY on the tea shop reviews provided below, recommend specific shops that match the user's request.

    CRITICAL RULES:
    - ONLY recommend shops that are explicitly mentioned in the reviews below
    - DO NOT make up or suggest shops that aren't in the provided reviews
    - If no shops match the request well, say so honestly
    - Always mention the specific shop names from the reviews

    User Request: {query}

    Tea Shop Reviews:
    {context}

    Provide 1-3 specific recommendations from the shops mentioned above. For each:
    1. State the shop name exactly as it appears in the reviews
    2. Explain why it matches based on the review content
    3. Quote specific details from the reviews

    Keep your response under 200 words and conversational."""

        response = self.model.generate_content(prompt)
        return response.text

# Singleton instance
_generator = None

def get_generator() -> LLMGenerator:
    global _generator
    if _generator is None:
        _generator = LLMGenerator()
    return _generator