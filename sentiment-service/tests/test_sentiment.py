from unittest.mock import patch, MagicMock
from app.sentiment import SentimentAnalyzer

@patch("app.sentiment.pipeline")
def test_sentiment_analyzer_success(mock_pipeline):
    mock_classifier = MagicMock()
    mock_classifier.return_value = [{"label": "joy", "score": 0.9856}]
    mock_pipeline.return_value = mock_classifier

    analyzer = SentimentAnalyzer()
    sentiment, confidence = analyzer.analyze("I am so happy today!")

    assert sentiment == "JOYFUL"
    assert confidence == 0.9856
    mock_classifier.assert_called_once_with("I am so happy today!")

@patch("app.sentiment.pipeline")
def test_sentiment_analyzer_fallback(mock_pipeline):
    mock_classifier = MagicMock()
    mock_classifier.side_effect = Exception("Model inference error")
    mock_pipeline.return_value = mock_classifier

    analyzer = SentimentAnalyzer()
    sentiment, confidence = analyzer.analyze("Failed text")

    assert sentiment is None
    assert confidence == 0.0
