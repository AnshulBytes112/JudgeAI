from fastapi.testclient import TestClient
from main import app, get_analyzer
import os
import tempfile
import pytest

client = TestClient(app)

class MockAnalyzer:
    def extract_audio(self, video_path, output_wav):
        # Create a dummy audio file so the next steps don't fail if they check
        with open(output_wav, "w") as f:
            f.write("dummy audio data")
            
    def analyze(self, audio_path):
        return {
            "transcript": "mock transcript",
            "wpm": 150.0,
            "filler_words": {
                "total": 0,
                "details": {}
            },
            "pauses": {
                "count": 0,
                "avg_duration": 0,
                "details": []
            },
            "duration_seconds": 10.0
        }

def override_get_analyzer():
    return MockAnalyzer()

app.dependency_overrides[get_analyzer] = override_get_analyzer

def test_analyze_speech_local_file():
    # Create a temporary dummy file to act as the local video
    with tempfile.NamedTemporaryFile(delete=False) as tmp:
        tmp.write(b"dummy video data")
        tmp_path = tmp.name

    try:
        response = client.post(
            "/analyze-speech",
            json={"path_or_url": tmp_path}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["transcript"] == "mock transcript"
        assert data["wpm"] == 150.0
    finally:
        if os.path.exists(tmp_path):
            os.remove(tmp_path)

def test_analyze_speech_missing_file():
    response = client.post(
        "/analyze-speech",
        json={"path_or_url": "nonexistent_file.mp4"}
    )
    assert response.status_code == 404
