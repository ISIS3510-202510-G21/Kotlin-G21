# GrowHub

GrowHub is a Kotlin-based application designed to foster and manage skill-development events. By connecting students, professionals, and organizers on a centralized platform, GrowHub helps users discover, register, and engage with both free and paid learning opportunities.

## Table of Contents

* [Value Proposition](#value-proposition)
* [Features](#features)
* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Running the Application](#running-the-application)
* [Test User Login](#test-user-login)
* [Technologies Used](#technologies-used)

## Value Proposition

GrowHub connects students, professionals, and organizers on a centralized platform to discover, register, and engage with skill-development events. By integrating personalized recommendations, networking tools, and event management features, GrowHub maximizes access to learning opportunities while enhancing visibility for organizers. Users find the right events, expand their professional network, and organizers reach their target audience effectively, creating a seamless and impactful experience for all.

## Features

* **Location-Based Discovery:** Shows events on a map and suggests nearby opportunities in real time using GPS.
* **Smart Recommendations:** Adapts to user interests by analyzing event participation patterns, going beyond simple keyword filtering.
* **Offline Support:** Core views (Profile, My Events, Search Events, Event Detail) work offline with graceful degradation, placeholders, and local data caching.
* **Advanced Filtering:** Filter events by type, category, location, and date to find exactly what you need.
* **Attendee Insights:** Review profiles of other attendees and view group statistics to make informed decisions.
* **Connectivity-Aware Chatbot:** Status indicators show online/offline state; input is disabled when offline to avoid failed interactions.
* **Interest-Based Onboarding:** Select your interests during registration to personalize the experience from day one.

## Prerequisites

* JDK 11 or higher installed
* Gradle (or use the Gradle wrapper included)
* IntelliJ IDEA or another Kotlin-compatible IDE

## Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/ISIS3510-202510-G21/Kotlin-G21.git
   cd Kotlin-G21
   ```
2. **Open the project** in your IDE
3. **Import Gradle settings** if prompted and allow dependencies to download

## Running the Application

Execute the app via Gradle:

```bash
./gradlew run
```

Or run the `main` function in `App.kt` (or your designated entry point) directly from the IDE.

## Test User Login

Use these credentials to explore GrowHub:

* **Email:** [camila@gmail.com](mailto:camila@gmail.com)
* **Password:** 1234567

> These credentials are for testing and demo purposes only.

## Technologies Used

* Kotlin
* Gradle
* Ktor (backend)
* Jetpack Compose (Android UI)
* Firestore (persistence)
