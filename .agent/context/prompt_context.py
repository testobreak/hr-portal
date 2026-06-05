# context/prompt_context.py

from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field


@dataclass
class PromptContext:
    goal: str = ""
    status: str = "active"
    current_step: str = ""
    completed_steps: List[str] = field(default_factory=list)
    failed_steps: List[str] = field(default_factory=list)
    pending_executions: List[Dict[str, Any]] = field(default_factory=list)
    current_plan: List[Dict[str, Any]] = field(default_factory=list)
    test_failures: List[str] = field(default_factory=list)
    repair_history: List[Dict[str, Any]] = field(default_factory=list)
    retrieved_chunks: List[Dict[str, Any]] = field(default_factory=list)
    active_symbols: List[str] = field(default_factory=list)
    touched_files: List[str] = field(default_factory=list)
    modification_targets: List[str] = field(default_factory=list)
    modified_files: List[str] = field(default_factory=list)
    generated_code: List[Dict[str, str]] = field(default_factory=list)
    reflections: List[str] = field(default_factory=list)
    architecture_notes: List[str] = field(default_factory=list)
    recent_tool_outputs: List[Dict[str, Any]] = field(default_factory=list)
    execution_history: List[Dict[str, Any]] = field(default_factory=list)
    runtime_memory: Dict[str, Any] = field(default_factory=dict)
    loop_warning: bool = False

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "PromptContext":
        kwargs = {}
        for field_name in cls.__dataclass_fields__:
            if field_name in data:
                kwargs[field_name] = data[field_name]
        return cls(**kwargs)
