from unittest.mock import patch
from fastapi.testclient import TestClient

with (
    patch("py_eureka_client.eureka_client.init_async"),
    patch("py_eureka_client.eureka_client.stop_async"),
    patch("app.sentiment.pipeline"),
):
    from app.main import app

client = TestClient(app)


@patch("app.main.analyzer")
def test_analyze_sentiment_success(mock_analyzer):
    mock_analyzer.analyze.return_value = ("JOYFUL", 0.95)

    response = client.post(
        "/analyze",
        json={"entry_id": "123", "content": "Today is great", "user_id": "user"},
    )

    assert response.status_code == 200
    data = response.json()
    assert data["entry_id"] == "123"
    assert data["sentiment"] == "JOYFUL"
    assert data["confidence"] == 0.95


def test_analyze_sentiment_empty_content():
    response = client.post(
        "/analyze", json={"entry_id": "123", "content": "  ", "user_id": "user"}
    )
    assert response.status_code == 400


def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "UP"
