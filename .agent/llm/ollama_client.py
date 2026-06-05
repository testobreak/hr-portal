# llm/ollama_client.py

import requests
import re
from requests.exceptions import (
    ConnectionError,
    Timeout,
    RequestException
)

class OllamaClient:

    DEFAULT_TIMEOUT = None  # Infinite timeout

    def __init__(
        self,
        model="qwen2.5-coder:7b",
        host="http://localhost:11434"
    ):
        self.model = model
        self.host = host.rstrip("/")

    def _resolve_model(self, requested_model):
        try:
            resp = requests.get(f"{self.host}/api/tags", timeout=10)
            if resp.status_code == 200:
                models_data = resp.json().get("models", [])
                available_models = [m["name"] for m in models_data]
            else:
                available_models = []
        except Exception:
            available_models = []

        if not available_models:
            return requested_model

        # 1. Exact match
        if requested_model in available_models:
            return requested_model

        # 2. Case-insensitive exact match
        for model in available_models:
            if model.lower() == requested_model.lower():
                return model

        # 3. Strip tags and check prefix/suffix
        req_base = requested_model.split(":")[0] if ":" in requested_model else requested_model
        
        # Look for prefix matches
        for model in available_models:
            if model.startswith(req_base) or req_base in model:
                return model
                
        # If nothing matches, return first available or default
        return available_models[0] if available_models else requested_model

    def _generate_fallback(self, model, messages):
        # Format messages into a single prompt string
        prompt_lines = []
        for msg in messages:
            role = msg.get("role", "user")
            content = msg.get("content", "")
            prompt_lines.append(f"{role.upper()}: {content}")
        prompt = "\n\n".join(prompt_lines)
        
        response = requests.post(
            f"{self.host}/api/generate",
            json={
                "model": model,
                "prompt": prompt,
                "stream": False
            },
            timeout=self.DEFAULT_TIMEOUT
        )
        response.raise_for_status()
        data = response.json()
        
        if "response" in data:
            return str(data["response"])
        elif "message" in data and isinstance(data["message"], dict):
            return str(data["message"].get("content", ""))
        else:
            raise RuntimeError("Ollama generate response missing 'response' or 'message'.")

    def _chat_raw(self, model, messages):
        try:
            response = requests.post(
                f"{self.host}/api/chat",
                json={
                    "model": model,
                    "messages": messages,
                    "stream": False
                },
                timeout=self.DEFAULT_TIMEOUT
            )
            
            # If 404, fallback to /api/generate
            if response.status_code == 404:
                return self._generate_fallback(model, messages)
                
            response.raise_for_status()
            
            data = response.json()
            message = data.get("message")
            if not isinstance(message, dict):
                raise RuntimeError("Ollama response missing 'message' object.")
            content = message.get("content")
            if content is None:
                raise RuntimeError("Ollama response missing 'content'.")
            return str(content)
            
        except Exception as e:
            # Check if this exception is due to a 404 HTTP status
            if isinstance(e, requests.RequestException) and getattr(e.response, "status_code", None) == 404:
                return self._generate_fallback(model, messages)
            raise e

    def chat(
        self,
        messages,
        role=None
    ):
        role_model_mapping = {
            "planner": "qwen2.5-coder:7b",
            "modifier": "qwen2.5-coder:7b-instruct",
            "critic": "qwen2.5-coder:7b",
            "repair": "qwen2.5-coder:7b",
        }
        requested_model = role_model_mapping.get(role, self.model)
        resolved_model = self._resolve_model(requested_model)
        
        prompt_text = "".join(m.get("content", "") for m in messages)
        chars = len(prompt_text)
        words = len(prompt_text.split())
        est_tokens = int(words * 1.3)
        
        print(f"[LLM] role={role} model={resolved_model} timeout=None chars={chars:,} words={words:,} est_tokens={est_tokens:,}")
        
        try:
            return self._chat_raw(resolved_model, messages)
        except Exception as e:
            if role == "modifier":
                print(f"[LLM] Primary modifier {resolved_model} failed. Retrying with fallback: deepseek-coder:6.7b...")
                fallback_model = self._resolve_model("deepseek-coder:6.7b")
                print(f"[LLM] role=modifier model={fallback_model} timeout=None chars={chars:,} words={words:,} est_tokens={est_tokens:,}")
                try:
                    return self._chat_raw(fallback_model, messages)
                except Exception as fallback_e:
                    raise RuntimeError(f"Ollama request failed: {fallback_e}") from fallback_e
            else:
                raise RuntimeError(f"Ollama request failed: {e}") from e

    # =====================================================
    # HEALTH CHECK
    # =====================================================

    def ping(
        self
    ):
        try:
            response = requests.get(
                f"{self.host}/api/tags",
                timeout=10
            )
            return (
                response.status_code == 200
            )
        except Exception:
            return False