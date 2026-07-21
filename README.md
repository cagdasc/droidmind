# DroidMind

## What is DroidMind?

**DroidMind** is an AI-powered automation framework that controls Android devices and emulators through natural language
commands. It uses large language models (LLMs) to interpret user intents and execute corresponding actions on connected Android devices.

## Project Goal

The project aims to provide an intelligent agent system that can:
- Understand natural language commands to perform Android device operations
- Automatically control UI elements through AI analysis of device layouts
- Execute complex multi-step automation tasks
- Support both local and cloud-based language models (e.g., Google Gemini)
- Provide a flexible, extensible framework for device automation

## Key Features

- **AI-Powered Device Control**: Uses LLMs (Gemini or local models) to execute device actions
- **ADB Integration**: Deep integration with Android Debug Bridge for device communication
- **Tool-Based Agent System**: Extensible framework for adding new device capabilities
- **Layout Analysis**: Analyzes device UI hierarchies for intelligent interaction
- **Verification Engine**: Confirms action outcomes through device state verification
- **Agent Client Protocol (ACP)**: Supports ACP integration for extensible agent capabilities
- **IntelliJ Integration**: Can be integrated into IntelliJ IDEA for IDE-native automation workflows
- **Kotlin Multiplatform**: Built in Kotlin for desktop environments

## Example Usage

    Find search button in toolbar and click it. Type "First video in YouTube" and send done event. Once you get search
    results get list in screen and click first element. After that tell me what did you do and what you see in the screen.

> [!CAUTION]
> This agent can execute ADB commands automatically in response to prompts, without confirmation. Use with care,
> especially for commands that modify app state or perform sensitive operations. Recommended for use in development
> environments or emulators.

License
-------

This project is licensed under the Apache License 2.0 – see the [LICENSE](LICENSE) file for details.