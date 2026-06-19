from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any
from sentence_transformers import SentenceTransformer
import numpy as np
import re
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="NLP Scoring Microservice")

# Global model instance
model = None

@app.on_event("startup")
def load_model():
    global model
    logger.info("Loading SentenceTransformer model...")
    model = SentenceTransformer('all-MiniLM-L6-v2')
    logger.info("Model loaded successfully.")

class ScoreRequest(BaseModel):
    transcript: str
    question: str
    ideal_answer: str
    keywords: List[str]

def compute_cosine_similarity(vec1, vec2):
    dot = np.dot(vec1, vec2)
    norm1 = np.linalg.norm(vec1)
    norm2 = np.linalg.norm(vec2)
    if norm1 == 0 or norm2 == 0:
        return 0.0
    return float(dot / (norm1 * norm2))

@app.post("/analyze-content")
async def analyze_content(request: ScoreRequest) -> Dict[str, Any]:
    try:
        transcript = request.transcript.strip()
        question = request.question.strip()
        ideal_answer = request.ideal_answer.strip()
        
        if not transcript or not ideal_answer:
            raise HTTPException(status_code=400, detail="Transcript and ideal_answer cannot be empty.")

        # 1. Semantic Similarity
        # Encode strings
        embeddings = model.encode([transcript, ideal_answer, question])
        trans_vec = embeddings[0]
        ideal_vec = embeddings[1]
        quest_vec = embeddings[2]

        ans_sim = compute_cosine_similarity(trans_vec, ideal_vec)
        quest_sim = compute_cosine_similarity(trans_vec, quest_vec) if question else 0.0

        # Combine semantic similarities (heavily weight the ideal answer, but penalize if totally off-topic to question)
        # Bounding between 0 and 1
        ans_sim = max(0.0, ans_sim)
        quest_sim = max(0.0, quest_sim)
        
        if question:
            semantic_score = (ans_sim * 0.7) + (quest_sim * 0.3)
        else:
            semantic_score = ans_sim

        # 2. Keyword Coverage
        matched_keywords = []
        missing_keywords = []
        
        # Regex token-based match with allowed suffixes
        # e.g. \bkeyword(?:s|es|ed|ing|js|boot)?\b
        for kw in request.keywords:
            kw_clean = kw.strip().lower()
            if not kw_clean:
                continue
                
            # Escape regex characters in keyword
            escaped_kw = re.escape(kw_clean)
            pattern = rf'\b{escaped_kw}(?:s|es|ed|ing|js|boot)?\b'
            
            if re.search(pattern, transcript.lower()):
                matched_keywords.append(kw)
            else:
                missing_keywords.append(kw)
                
        total_kw = len(request.keywords)
        keyword_coverage = len(matched_keywords) / total_kw if total_kw > 0 else 1.0

        # 3. Overall Score
        # MVP Formula: 0.6 * semantic_similarity + 0.4 * keyword_coverage
        overall_score = (0.6 * semantic_score) + (0.4 * keyword_coverage)
        
        return {
            "overallScore": round(overall_score * 100),
            "semanticSimilarity": round(semantic_score * 100),
            "keywordCoverage": round(keyword_coverage * 100),
            "matchedKeywords": matched_keywords,
            "missingKeywords": missing_keywords,
            "details": {
                "answer_similarity_raw": round(ans_sim, 3),
                "question_similarity_raw": round(quest_sim, 3)
            }
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error during NLP scoring: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
