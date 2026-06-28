from transformers import pipeline
import logging

logger = logging.getLogger(__name__)

LABEL_MAP = {
    "joy": "JOYFUL",
    "sadness": "SAD",
    "anger": "ANGRY",
    "disgust": "DISGUSTED",
    "fear": "ANXIOUS",
    "surprise": "SURPRISED",
    "neutral": "NEUTRAL",
}


class SentimentAnalyzer:
    def __init__(self):
        logger.info("Loading HuggingFace sentiment model...")
        self.classifier = pipeline(
            "sentiment-analysis", model="j-hartmann/emotion-english-distilroberta-base"
        )
        logger.info("Model loaded successfully!")

    def analyze(self, text: str) -> tuple[str, float]:
        """Returns (sentiment_label, confidence_score)."""
        try:
            result = self.classifier(text[:512])[
                0
            ]  # Truncate to 512 tokens (model limit)
            label = result["label"]
            score = result["score"]
            sentiment = LABEL_MAP.get(label.lower(), "NEUTRAL")
            return sentiment, round(score, 4)
        except Exception as e:
            logger.error(f"Sentiment analysis failed: {e}")
            return None, 0.0
