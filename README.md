# Human Pose Estimation for Physiotherapy Applications 

## Overview
Human Pose Estimation for Physiotherapy Applications is an innovative Android app designed to enhance physiotherapy treatment by leveraging advanced computer vision and machine learning technologies. The app's primary functionality includes real-time tracking and analysis of human body movements to assist physiotherapists in monitoring patient exercises and progress. The app uses smartphone cameras to capture video input, which is then processed to estimate the patient's pose using key-point detection.

This app is designed to run on a physical Android device to utilize its camera functionality. It employs **Mediapipe’s Pose Landmarker model** for pose estimation and includes comprehensive features tailored for both patients and doctors.

## Features
### General
- **Human Pose Estimation**: Real-time tracking and analysis of human body movements using the Mediapipe Pose Landmarker model.
- **User Authentication**: Secure sign-up and sign-in for two user types (**Patient** and **Doctor**) using **Firebase Authentication**.

### Patient Features
- Perform three types of exercises: **Elbow, Knee, and Shoulder exercises**.
- Tracks and stores:
  - Maximum and Minimum angles during exercises.
  - Maximum repetitions (reps).
  - Date and time of exercise sessions.
- Exercise data is securely stored in **Firestore Database** for tracking progress.

### Doctor Features
- **Patient Management**: Search for patients and add them to a personalized **Patient List**.
- **Data Insights**:
  - View detailed exercise data for each patient.
  - Access weekly reports to analyze patient progress.
- **Analysis and Prescription**: Exercise data assists doctors in tailoring physiotherapy prescriptions.

## Implementation Details
- **Technologies and Frameworks**:
  - **Mediapipe Pose Landmarker** for pose estimation.
  - **Firebase Authentication** for secure user login.
  - **Firestore Database** for storing exercise and user data.
  - **Firebase Storage** for user profile image storage.
  - **CameraX** for capturing real-time video during exercises.
  - **Canvas** for visualizing pose data with lines and angles.
- **Architecture**: 
  - **MVVM Architecture**: Ensures clean separation of concerns and scalability.
- **UI Components**:
  - **RecyclerViews** for dynamic lists.
  - **Navigation Drawers** for intuitive app navigation.

## How It Works
1. **Pose Estimation**:
   - The app uses the Mediapipe Pose Landmarker model to estimate the user’s pose and retrieve key points.
   - Real-time analysis calculates angles and tracks exercise progress.

2. **Patient Workflow**:
   - Patients log in, select an exercise, and start their session.
   - The app calculates exercise angles, tracks reps, and uploads data to Firestore.

3. **Doctor Workflow**:
   - Doctors log in to view their patient list.
   - They can search for patients, view exercise history, and analyze weekly reports.

## Getting Started
1. Clone the repository:
   ```bash
   git clone https://github.com/JayeshBainwad/FlexiPose.git
   ```
2. Open the project in **Android Studio**.
3. Add your **Firebase configuration file** (`google-services.json`).
4. Build and run the app on a physical device.

## Future Enhancements
- Add more exercise types and custom exercise plans.
- Implement AI-based progress prediction for patients.
- Enhance the UI/UX with Material3 design components.
- Introduce video playback for patient progress comparison.

## Conclusion
This app demonstrates the effective integration of advanced technologies like Mediapipe, Firebase, and CameraX to create a real-world solution for physiotherapy. Its dual focus on patient and doctor functionalities provides a streamlined experience, supporting enhanced treatment and monitoring.

