package com.cacaosd.droidmind.domain.tools

object UiInteractionToolsConstant {
    const val TOOLSET_DESCRIPTION =
        "A set of tools to interact with the device's UI."

    const val INPUT_TEXT_TOOL = "input_text"
    const val INPUT_TEXT_TOOL_DESC =
        "Types and sends text input to the Android device using the ADB shell input command."

    const val TAP_TOOL = "tap"
    const val TAP_TOOL_DESC =
        "Taps at the specified (x, y) screen coordinates on the device. If multiple is requested, send tapCount as a parameter"

    const val SEND_KEY_EVENT_TOOL = "send_key_event"
    const val SEND_KEY_EVENT_TOOL_DESC =
        "Sends a key event to the Android device. Supported keys include: home, back, menu, search, enter, done, next, del, space, tab, up, down, left, right."

    const val SCROLL_DOWN_TOOL = "scroll_down"
    const val SCROLL_DOWN_TOOL_DESC =
        "Executes a vertical scroll down by moving from (startX, startY) to (endX, endY) where startX == endX and endY > startY, moving the finger downward on the screen."

    const val SCROLL_UP_TOOL = "scroll_up"
    const val SCROLL_UP_TOOL_DESC =
        "Executes a vertical scroll up by moving from (startX, startY) to (endX, endY) where startX == endX and endY < startY, moving the finger upward on the screen."

    const val SCROLL_LEFT_TOOL = "scroll_left"
    const val SCROLL_LEFT_TOOL_DESC =
        "Executes a horizontal scroll left by moving from (startX, startY) to (endX, endY) where startY == endY and endX < startX, moving the finger toward the left side of the screen."

    const val SCROLL_RIGHT_TOOL = "scroll_right"
    const val SCROLL_RIGHT_TOOL_DESC =
        "Executes a horizontal scroll right by moving from (startX, startY) to (endX, endY) where startY == endY and endX > startX, moving the finger toward the right side of the screen."
}
