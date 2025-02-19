# Human Pose Estimation for Physiotherapy Applications  

## Overview  
**Human Pose Estimation for Physiotherapy Applications** is an innovative Android app designed to enhance physiotherapy treatment by leveraging advanced computer vision and machine learning technologies. The app provides real-time tracking and analysis of human body movements, assisting physiotherapists in monitoring patient exercises and progress. Using smartphone cameras, the app captures video input and processes it to estimate the patient's pose using key-point detection.  

This app is designed to run on a physical Android device to utilize its camera functionality. It employs **Mediapipe’s Pose Landmarker model** for pose estimation and includes comprehensive features tailored for both patients and doctors.  

🚀 **Built with Kotlin and XML** for Android development.  

---

## Features  

### **General Features**  
- **Human Pose Estimation**: Real-time tracking and analysis of human body movements using the Mediapipe Pose Landmarker model.  
- **User Authentication**: Secure sign-up and sign-in for two user types (**Patient** and **Doctor**) using **Firebase Authentication**.  

### **Patient Features**  
- Perform three types of exercises: **Elbow, Knee, and Shoulder exercises**.  
- Tracks and stores:  
  - Maximum and Minimum angles during exercises.  
  - Maximum repetitions (reps).  
  - Date and time of exercise sessions.  
- Exercise data is securely stored in **Firestore Database** for tracking progress.  

### **Doctor Features**  
- **Patient Management**: Search for patients and add them to a personalized **Patient List**.  
- **Data Insights**:  
  - View detailed exercise data for each patient.  
  - Access weekly reports to analyze patient progress.  
- **Analysis and Prescription**: Exercise data assists doctors in tailoring physiotherapy prescriptions.  

---

## Implementation Details  

### **Technologies and Frameworks**  
- **Kotlin and XML** for Android development.  
- **Mediapipe Pose Landmarker** for pose estimation.  
- **Firebase Authentication** for secure user login.  
- **Firestore Database** for storing exercise and user data.  
- **Firebase Storage** for user profile image storage.  
- **CameraX** for capturing real-time video during exercises.  
- **Canvas** for visualizing pose data with lines and angles.  

### **Architecture**  
- **MVVM Architecture**: Ensures clean separation of concerns and scalability.  

### **UI Components**  
- **RecyclerViews** for dynamic lists.  
- **Navigation Drawers** for intuitive app navigation.  

---

## How It Works  

### **Pose Estimation**  
- The app uses the Mediapipe Pose Landmarker model to estimate the user’s pose and retrieve key points.  
- Real-time analysis calculates angles and tracks exercise progress.  

### **Patient Workflow**  
1. Patients log in and select an exercise.  
2. The app calculates exercise angles, tracks reps, and uploads data to Firestore.  

### **Doctor Workflow**  
1. Doctors log in to view their patient list.  
2. They can search for patients, view exercise history, and analyze weekly reports.  

---

## Screenshots  

![ss1](https://github.com/user-attachments/assets/c99b5f12-9ed6-4146-a378-5c6103e19896)  
![ss2](https://github.com/user-attachments/assets/ccf3f48e-741c-4ff1-a418-b6a153617922)  
![ss3](https://github.com/user-attachments/assets/317b227d-781f-4c78-a394-4abe4de57c1b)  
![ss4](https://github.com/user-attachments/assets/69abbeee-ae86-47df-9d0b-3d0abdcae5c8)  
![ss5](https://github.com/user-attachments/assets/d4002287-b178-45a8-a353-4d4305fe6a16)  
![ss6](https://github.com/user-attachments/assets/fa64acdc-5879-4ba6-97a2-52ab8364dca0)  
![ss7](https://github.com/user-attachments/assets/ed05d1cc-7238-450b-9994-3ef421d83450)  
![ss8](https://github.com/user-attachments/assets/854febdd-887f-4dab-a2cc-fbe08a005cd8)  
![ss9](https://github.com/user-attachments/assets/33b1bef9-a349-4ae5-9f49-402953bac071)  
![ss10](https://github.com/user-attachments/assets/f2b713fb-d5c8-40f3-a4a8-3c329394c2a6)  

---

## Getting Started  

1. Clone the repository:  
   ```bash  
   git clone https://github.com/JayeshBainwad/FlexiPose.git  
   ```  
2. Open the project in **Android Studio**.  
3. **Add your Firebase configuration file (`google-services.json`).**  
   - ⚠️ If you want to share this project with others, document in the README that they need to add their own Firebase configuration.  
4. Build and run the app on a physical device.  

---

| Dependency | Version |
|------------|---------|
| **Compile SDK** | 34 |
| **Min SDK** | 26 |
| **Kotlin** | Latest stable |
| **Firebase BoM** | 33.4.0 |
| **Firebase Auth** | 23.0.0 |
| **Firestore** | 25.1.0 |
| **Firebase Storage** | 21.0.1 |
| **MediaPipe Tasks Vision** | 0.20230731 |
| **CameraX Core** | 1.2.0-alpha02 |
| **Navigation Component** | 2.5.3 |
| **AppCompat** | 1.7.0 |
| **Material Components** | 1.12.0 |
| **Glide (Image Loading)** | 4.11.0 |

---

## Future Enhancements  
- Add more exercise types and custom exercise plans.  
- Implement AI-based progress prediction for patients.  
- Enhance the UI/UX with Material3 design components.  
- Introduce video playback for patient progress comparison.  

---

## Conclusion  
This app demonstrates the effective integration of advanced technologies like Mediapipe, Firebase, and CameraX to create a real-world solution for physiotherapy. Its dual focus on patient and doctor functionalities provides a streamlined experience, supporting enhanced treatment and monitoring.  

---

## License  
This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for more information.  

This project includes code adapted from the original MediaPipe repository for **PoseLandmarkerHelper.kt**, **build.gradle (Module :app)**, and **download_tasks.gradle (Module :app)** with minor modifications. The original code is licensed under the Apache License, Version 2.0.  

---
