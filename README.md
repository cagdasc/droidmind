DroidMind
=========

DroidMind is a Kotlin project that enables control of Android devices or emulators through an AI agent. The agent
processes natural language commands and performs corresponding actions on connected Android devices—such as listing
installed apps, launching applications, and interacting with UI elements. It supports integration with local or cloud
based language models.

Features
--------

- AI-powered device control using Gemini or local models.
- Integration with ADB (Android Debug Bridge).
- Tool-based agent for device actions.
- Extensible with new tools and capabilities.
- Written in Kotlin Multiplatform for desktop execution.

Caution
-------

> [!CAUTION]
> This agent can execute ADB commands automatically in response to prompts, without confirmation. Use with care,
> especially for commands that modify app state or perform sensitive operations. Recommended for use in development
> environments or emulators.

Demo
----

Example prompt:

    Find search button in toolbar and click it. Type "First video in YouTube" and send done event. Once you get search 
    results get list in screen and click first element. After that tell me what did you do and what you see in the screen.

    1. Find search button in toolbar and click it. Then verify "Search YouTube" text.
    
    2. Type "First video in YouTube" and send done event. Then verify you see "Me at the zoo" video
    
    3. Find item which is "Me at the zoo" and click it. Then verify you seee "Me at zoo" title and publisher name is "jawed"

See demo video (or go [demo](demo) directory):
[![Video thumbnail](demo/demo_cover.png)](https://github.com/user-attachments/assets/85943f11-f6da-4352-9e14-4c15f83ecedc)

Getting Started
---------------

Prerequisites:

- Java Development Kit (JDK) 17 or higher
- IntelliJ IDEA or any Kotlin-compatible IDE with Gradle support
- Android SDK and ADB (Android Debug Bridge)
    - Install ADB via Android Studio or platform-tools
- Android emulator or device connected and recognized by `adb devices`

Roadmap
-------

- Improve UI dump quality and detail
- Support richer component hierarchies
- Add more ADB-based tools
- Improve error handling for device communication
- Refine layout structures for better LLM understanding
- Add a verification engine to confirm action outcomes
- Investigate migration from ddmlib to adblib

License
-------

This project is licensed under the Apache License 2.0 – see the [LICENSE](LICENSE) file for details.
