from pydantic_settings import BaseSettings
from functools import lru_cache

class Settings(BaseSettings):
    google_api_key: str
    gemini_model: str = "gemini-1.5-flash"
    embedding_model: str = "models/embedding-001"
    vector_db_type: str = "chroma"
    chroma_persist_dir: str = "./data/chroma_db"
    java_service_url: str = "http://localhost:8081"
    
    class Config:
        env_file = ".env"

@lru_cache()
def get_settings():
    return Settings()