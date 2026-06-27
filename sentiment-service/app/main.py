import os
import logging
from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager
import py_eureka_client.eureka_client as eureka_client

from .models import SentimentRequest, SentimentResponse
from .sentiment import SentimentAnalyzer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

analyzer: SentimentAnalyzer = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup and shutdown events."""
    global analyzer
    analyzer = SentimentAnalyzer()

    # Register with Eureka
    eureka_server = os.getenv("EUREKA_URI", "http://localhost:8761/eureka/")

    try:
        await eureka_client.init_async(
            eureka_server=eureka_server,
            app_name="SENTIMENT-SERVICE",
            instance_port=8083,
            instance_host=os.getenv("HOSTNAME", "localhost"),
        )
        logger.info("Registered with Eureka successfully")
    except Exception as e:
        logger.error(f"Failed to register with Eureka: {e}")

    yield  # App is running

    try:
        await eureka_client.stop_async()
        logger.info("De-registered from Eureka")
    except Exception as e:
        logger.error(f"Error during Eureka de-registration: {e}")


app = FastAPI(
    title="MindScribe Sentiment Analysis Service",
    description="MindScribe NLP emotion & sentiment analysis using HuggingFace DistilRoBERTa",
    lifespan=lifespan,
)


@app.post("/analyze", response_model=SentimentResponse)
async def analyze_sentiment(request: SentimentRequest):
    """Analyze the sentiment of journal entry content."""
    if not request.content or not request.content.strip():
        raise HTTPException(status_code=400, detail="Content cannot be empty")

    sentiment, confidence = analyzer.analyze(request.content)
    if sentiment is None:
        raise HTTPException(status_code=500, detail="Analysis failed")

    return SentimentResponse(
        entry_id=request.entry_id,
        sentiment=sentiment,
        confidence=confidence,
    )


@app.get("/health")
async def health_check():
    return {"status": "UP", "model": "j-hartmann/emotion-english-distilroberta-base"}
