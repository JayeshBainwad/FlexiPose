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

![cae957f6-fa96-46cd-aefd-7fa2ba3cb86c](https://github.com/user-attachments/assets/fa8176ec-bd09-4a29-a75f-5566cd6a68b7)
![e2edbacd-10fa-4551-95b1-c5b43a0a8b9d](https://github.com/user-attachments/assets/af2fabc3-68a5-4111-9b46-2940d6c9f7a0)
![c4379e33-1dd1-4dd7-a381-9e99e80adbce](https://github.com/user-attachments/assets/aa19c23b-66b2-4eb1-86f9-b2e69e11a83a)
![a3324b1e-9101-4510-935e-bbe9e696057d](https://github.com/user-attachments/assets/2b3d34ac-0902-48da-8bfd-344522db87f5)
![01725c32-9ab4-41ee-8eaa-0d3d968094c8](https://github.com/user-attachments/assets/cc119998-40e1-44cc-a9c8-4c19eee01c52)
![15c185af-2a79-4fa7-891c-c0a777103176](https://github.com/user-attachments/assets/c80b8f45-fd8c-4f8d-92c8-efaf7b7ae893)
![8eb6a86e-a4c4-4d46-81ba-4c11a0eb8fce](https://github.com/user-attachments/assets/b9137ac7-f370-4560-8f97-5e43aa5db295)
![e2edbacd-10fa-4551-95b1-c5b43a0a8b9d](https://github.com/user-attachments/assets/8a454f5b-8fe8-43fc-9e88-06a2006ffe25)
![0483f78d-b976-4a7f-b750-42b64536d3a9](https://github.com/user-attachments/assets/b866e422-00ea-4faa-b976-e2219622381b)
![a0e09dfd-2db8-469e-a6a0-a77678894556](https://github.com/user-attachments/assets/a0c3e9f6-faac-4304-b92b-12cc0c572b50)
![b9883b74-4b58-457c-a2a5-305b959b4a75](https://github.com/user-attachments/assets/ccc05f5f-e282-4a85-8479-ddf02d0c23c1)
![0a606664-0024-4c62-a8a6-fb715efa81be](https://github.com/user-attachments/assets/9f767465-f6df-4f1b-a28a-9af5389ab8f9)
![d2f18014-8972-4810-b6f9-66a7430e5685](https://github.com/user-attachments/assets/3757d387-d696-4157-ab16-533859ac6bf3)
![f6704fd1-8553-4f0b-98bc-7ff9f62923b7](https://github.com/user-attachments/assets/2ac0abbb-a730-4bc5-84a7-f0e73dd14ff6)

