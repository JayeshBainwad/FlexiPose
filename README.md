# Human Pose Estimation for Physiotherapy Applications 

### Overview

Human Pose Estimation for Physiotherapy Applications is an innovative Android app designed to enhance physiotherapy treatment by leveraging advanced computer vision and machine learning technologies. The app's primary functionality includes real-time tracking and analysis of human body movements to assist physiotherapists in monitoring patient exercises and progress. The app uses smartphone cameras to capture video input, which is then processed to estimate the patient's pose using key-point detection

This is a camera app that can detect landmarks on a person either from continuous camera frames seen by your device's back camera, an image, or a video from the device's gallery using a custom **task** file.

The task file is downloaded by a Gradle script when you build and run the app. You don't need to do any additional steps to download task files into the project explicitly unless you wish to use your own landmark detection task. If you do use your own task file, place it in the app's *assets* directory.

This application should be run on a physical Android device to take advantage of the camera.
