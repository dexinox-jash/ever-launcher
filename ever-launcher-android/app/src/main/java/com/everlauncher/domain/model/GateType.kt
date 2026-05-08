package com.everlauncher.domain.model

enum class GateType {
    /** 5-second breathing animation, auto-dismisses */
    BREATHING,
    /** Text input "What are you opening X for?" — min 5 chars to continue */
    INTENTION,
    /** 10-second countdown, no skip */
    DELAY
}
