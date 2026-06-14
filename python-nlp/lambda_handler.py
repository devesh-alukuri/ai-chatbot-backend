"""
AWS Lambda Handler - NLP Processing Engine
Handles intent classification and entity extraction
Author: Devesh Alukuri
"""

import json
import re
from collections import defaultdict

INTENTS = {
    "ORDER_STATUS_QUERY": ["order status", "track order", "where is my order", "order tracking"],
    "ORDER_CANCEL": ["cancel order", "cancel my order"],
    "RETURN_REFUND": ["return", "refund", "money back"],
    "PRICE_INQUIRY": ["price", "cost", "how much", "pricing"],
    "GREETING": ["hello", "hi", "hey", "good morning", "good evening"],
    "FAREWELL": ["bye", "goodbye", "thank you", "thanks"],
    "SUPPORT_REQUEST": ["help", "support", "issue", "problem", "not working"],
    "PRODUCT_INQUIRY": ["product", "item", "available", "in stock"],
    "PAYMENT_INQUIRY": ["payment", "pay", "invoice", "billing"],
}

RESPONSES = {
    "GREETING": "Hello! Welcome to our support center. How can I assist you today?",
    "FAREWELL": "Thank you for contacting us. Have a great day!",
    "ORDER_STATUS_QUERY": "I've located your order. It is currently out for delivery and expected within 24 hours.",
    "ORDER_CANCEL": "I can help cancel your order. Please provide your order number to proceed.",
    "RETURN_REFUND": "Our return policy allows returns within 30 days. Please provide your order number.",
    "PRICE_INQUIRY": "Could you specify the product you're asking about so I can give you accurate pricing?",
    "SUPPORT_REQUEST": "I'm sorry you're experiencing an issue. Could you describe the problem in detail?",
    "PRODUCT_INQUIRY": "We have many products available. What specific product or category are you looking for?",
    "PAYMENT_INQUIRY": "We accept credit cards, UPI, and net banking. What's your payment-related question?",
    "GENERAL_QUERY": "Thank you for your message. Could you provide more details so I can assist you better?"
}


def classify_intent(message: str) -> tuple:
    """Classify intent and return (intent, confidence)."""
    message_lower = message.lower()
    best_intent = "GENERAL_QUERY"
    best_score = 0

    for intent, keywords in INTENTS.items():
        score = sum(1 for kw in keywords if kw in message_lower)
        if score > best_score:
            best_score = score
            best_intent = intent

    confidence = min(0.95, 0.60 + best_score * 0.15) if best_score > 0 else 0.55
    return best_intent, round(confidence, 4)


def extract_entities(message: str) -> dict:
    """Extract named entities from message."""
    entities = {}

    order_match = re.search(r'#?(ORD-?)?\d{4,10}', message.upper())
    if order_match:
        entities["orderNumber"] = order_match.group()

    email_match = re.search(r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}', message)
    if email_match:
        entities["email"] = email_match.group()

    phone_match = re.search(r'\+?\d[\d\s-]{8,12}\d', message)
    if phone_match:
        entities["phone"] = phone_match.group().strip()

    return entities


def generate_response(intent: str, entities: dict, history: list) -> str:
    """Generate context-aware response."""
    base_response = RESPONSES.get(intent, RESPONSES["GENERAL_QUERY"])

    if intent == "ORDER_STATUS_QUERY" and "orderNumber" in entities:
        return f"I've checked {entities['orderNumber']}. It is out for delivery and arrives within 24 hours."

    # Add context from history if available
    if len(history) > 2 and intent == "GENERAL_QUERY":
        return "Based on our conversation, it seems you need more help. Let me connect you with a human agent."

    return base_response


def handler(event, context=None):
    """AWS Lambda entry point."""
    try:
        message = event.get("message", "")
        history = event.get("history", [])

        if not message:
            return {"error": "No message provided", "statusCode": 400}

        intent, confidence = classify_intent(message)
        entities = extract_entities(message)
        response = generate_response(intent, entities, history)

        return {
            "statusCode": 200,
            "intent": intent,
            "confidence": confidence,
            "entities": entities,
            "response": response,
            "processed": True
        }
    except Exception as e:
        return {"statusCode": 500, "error": str(e)}


if __name__ == "__main__":
    # Local test
    test_events = [
        {"message": "Hi there!", "history": []},
        {"message": "Where is my order #12345?", "history": []},
        {"message": "I want to return my item", "history": []},
        {"message": "What is the price of the laptop?", "history": []},
    ]
    print("=== Lambda NLP Test ===\n")
    for event in test_events:
        result = handler(event)
        print(f"Input : {event['message']}")
        print(f"Intent: {result['intent']} ({result['confidence']})")
        print(f"Response: {result['response']}")
        print("-" * 50)
