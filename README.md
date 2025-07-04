# 📱 Tracko: A User-Incentive Mobile Security System

Tracko is a mobile security application designed to empower users with control over their privacy and security through **real-time alerts**, **categorized permissions**, and a **reward-based dashboard**. Developed using **Kotlin** and **Java** for Android, Tracko introduces a user-centric security model that educates and incentivizes users to maintain safer digital habits.


## 🚀 Features

### 🔒 Phone Incentive Dashboard
- A **point-based incentive system** encouraging users to adopt and maintain strong security habits.
- Users earn or lose points based on:
  - Phone password usage
  - Biometric lock (fingerprint, face ID)
  - Avoidance of third-party app sources
- Points are dynamically updated and reflected in the security score (0–100), converted to a safety tier.

### 📊 Scoring System
| Score Range     | Security Level     |
|------------------|---------------------|
| 0–30             | Danger              |
| 31–50            | Weak                |
| 51–70            | Moderate            |
| 71–90            | Safe                |
| 91–100           | Fully Protected     |

> ⚠️ **Note:** Due to Android OS limitations, full real-time monitoring is not yet implemented.

### 🧠 Categorized Permissions
- Apps are auto-classified into 23 predefined categories (e.g., Social Media, Travel, Finance).
- Each category has default permissions, which users can fully customize.
- Create new categories or reassign apps to enhance security preferences.

### 🛠️ Sensitivity Control (Drag-and-Drop)
- App permissions are grouped by sensitivity:
  - High (60 points)
  - Moderate (30 points)
  - Low (10 points)
- Users can reassign functions between categories and adjust risk dynamically.

## 🛠️ Development Stack

- **Platform:** Android
- **Languages:** Kotlin, Java
- **Architecture:** Modular with a focus on extensibility and performance
- **Prototype Modules:**
  - Dashboard with real-time score updates
  - Category-based permission manager


## 🧪 Limitations & Future Plans

- Real-time permission tracking is constrained by OS limitations.
- Weekly password change detection is not supported by Android.
- Manual categorization of apps is required (no access to Google API yet).
- Currently tracks only a limited set of permissions (Wi-Fi, Bluetooth, Internet).

### ✅ Future Enhancements
- Integrate Google API for auto-categorization.
- Expand permission tracking to more device features.
- Add iOS support.

---

## 📎 Example Categories & Permissions

| Category        | Example Apps            | Default Permissions        |
|----------------|-------------------------|----------------------------|
| Social Media    | Instagram, TikTok       | Camera, Gallery, Files     |
| Travel          | Uber, Airbnb            | Location                   |
| Shopping        | Amazon, Daraz           | -                          |
| Finance         | bKash, Rocket           | Contacts                   |
| Health & Fitness| MyFitnessPal            | Bluetooth                  |
| Communication   | WhatsApp, Messenger     | Camera, Mic, Gallery, Files|

---

## 🧠 Why Tracko?

> “Tracko doesn’t just protect your phone; it teaches you *why* and *how* to secure your privacy.”

- Informs users of risk in real-time
- Customizable to individual needs
- Makes data security intuitive, rewarding, and engaging

