import whisper
import subprocess
import os
import re
import logging
from typing import Dict, List, Any

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class SpeechAnalyzer:
    def __init__(self, model_name: str = "base"):
        logger.info(f"Loading Whisper model: {model_name}")
        self.model = whisper.load_model(model_name)
        self.filler_words = ["um", "uh", "like", "you know"]

    def extract_audio(self, video_path: str, output_wav: str):
        """Extracts audio from video using FFmpeg."""
        logger.info(f"Extracting audio from {video_path} to {output_wav}")
        command = [
            "ffmpeg", "-i", video_path,
            "-vn", "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
            output_wav, "-y"
        ]
        try:
            subprocess.run(command, check=True, capture_output=True)
        except subprocess.CalledProcessError as e:
            logger.error(f"FFmpeg error: {e.stderr.decode()}")
            raise Exception(f"Failed to extract audio: {e.stderr.decode()}")

    def analyze(self, audio_path: str) -> Dict[str, Any]:
        """Transcribes audio and calculates metrics."""
        logger.info("Starting transcription...")
        result = self.model.transcribe(audio_path, verbose=False)
        
        transcript = result["text"].strip()
        segments = result["segments"]
        
        # Calculate duration
        duration_seconds = segments[-1]["end"] if segments else 0
        
        # Calculate WPM
        word_count = len(transcript.split())
        wpm = (word_count / (duration_seconds / 60)) if duration_seconds > 0 else 0
        
        # Detect filler words
        filler_counts = {}
        total_fillers = 0
        for filler in self.filler_words:
            count = len(re.findall(r'\b' + re.escape(filler) + r'\b', transcript, re.IGNORECASE))
            filler_counts[filler] = count
            total_fillers += count
            
        # Analyze pauses (gaps between segments)
        pauses = []
        for i in range(len(segments) - 1):
            pause_dur = segments[i+1]["start"] - segments[i]["end"]
            if pause_dur > 0.5: # Consider a pause if longer than 0.5 seconds
                pauses.append({
                    "start": segments[i]["end"],
                    "end": segments[i+1]["start"],
                    "duration": round(pause_dur, 2)
                })
        
        avg_pause = sum(p["duration"] for p in pauses) / len(pauses) if pauses else 0
        
        return {
            "transcript": transcript,
            "wpm": round(wpm, 2),
            "filler_words": {
                "total": total_fillers,
                "details": filler_counts
            },
            "pauses": {
                "count": len(pauses),
                "avg_duration": round(avg_pause, 2),
                "details": pauses
            },
            "duration_seconds": round(duration_seconds, 2)
        }
