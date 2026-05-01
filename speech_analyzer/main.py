from fastapi import FastAPI, HTTPException, Body
from pydantic import BaseModel
import os
import uuid
import requests
import boto3
from urllib.parse import urlparse
from analyzer import SpeechAnalyzer
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Speech Analysis Microservice")
analyzer = SpeechAnalyzer(model_name="base")

class AnalysisRequest(BaseModel):
    path_or_url: str

@app.post("/analyze-speech")
async def analyze_speech(request: AnalysisRequest):
    input_path = request.path_or_url
    job_id = str(uuid.uuid4())
    temp_video = f"temp_{job_id}.video"
    temp_audio = f"temp_{job_id}.wav"
    
    try:
        # 1. Acquire the file
        if input_path.startswith(("http://", "https://")):
            if "s3.amazonaws.com" in input_path or input_path.startswith("s3://"):
                download_from_s3(input_path, temp_video)
            else:
                download_from_url(input_path, temp_video)
            video_to_process = temp_video
        else:
            if not os.path.exists(input_path):
                throw_error(404, f"Local file not found: {input_path}")
            video_to_process = input_path

        # 2. Extract Audio
        analyzer.extract_audio(video_to_process, temp_audio)
        
        # 3. Analyze
        results = analyzer.analyze(temp_audio)
        
        return results

    except Exception as e:
        logger.error(f"Analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
    
    finally:
        # Cleanup temp files
        for f in [temp_video, temp_audio]:
            if os.path.exists(f) and f != input_path:
                os.remove(f)

def download_from_url(url: str, dest: str):
    logger.info(f"Downloading from URL: {url}")
    with requests.get(url, stream=True) as r:
        r.raise_for_status()
        with open(dest, 'wb') as f:
            for chunk in r.iter_content(chunk_size=8192):
                f.write(chunk)

def download_from_s3(url: str, dest: str):
    logger.info(f"Downloading from S3: {url}")
    s3 = boto3.client('s3')
    parsed = urlparse(url)
    bucket = parsed.netloc.split('.')[0]
    key = parsed.path.lstrip('/')
    s3.download_file(bucket, key, dest)

def throw_error(code: int, message: str):
    raise HTTPException(status_code=code, detail=message)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
