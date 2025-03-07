package com.google.mediapipe.examples.poselandmarker.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.mediapipe.examples.poselandmarker.activities.patient.CameraActivity
import com.google.mediapipe.examples.poselandmarker.activities.doctor.DoctorMainActivity
import com.google.mediapipe.examples.poselandmarker.activities.doctor.DoctorProfileActivity
import com.google.mediapipe.examples.poselandmarker.activities.doctor.DoctorSignInActivity
import com.google.mediapipe.examples.poselandmarker.activities.doctor.DoctorSignUpActivity
import com.google.mediapipe.examples.poselandmarker.activities.patient.MainActivity
import com.google.mediapipe.examples.poselandmarker.activities.patient.MyProfileActivity
import com.google.mediapipe.examples.poselandmarker.activities.patient.SignInActivity
import com.google.mediapipe.examples.poselandmarker.activities.patient.SignUpActivity
import com.google.mediapipe.examples.poselandmarker.model.Doctor
import com.google.mediapipe.examples.poselandmarker.model.Exercise
import com.google.mediapipe.examples.poselandmarker.utils.Constants
import com.google.mediapipe.examples.poselandmarker.model.Patient

/**
 * A custom class where we will add the operation performed for the firestore database.
 */
class FirestoreClass {

    // Create a instance of Firebase Firestore
    private val mFireStore = FirebaseFirestore.getInstance()

    /**
     * A function to make an entry of the registered user in the firestore database.
     */
    fun registerUser(activity: SignUpActivity, patientInfo: Patient) {

        mFireStore.collection(Constants.PATIENTUSERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(patientInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName,
                    "Error writing document", e)
            }
    }


    fun registerUserDoctor(activity: DoctorSignUpActivity, doctorInfo: Doctor) {

        mFireStore.collection(Constants.DOCTORUSERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(doctorInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName,
                    "Error writing document", e)
            }
    }

    fun getAllExercisesForPatient(patientId: String, onComplete: (List<Exercise>) -> Unit) {
        val exerciseCollectionRef = mFireStore.collection(Constants.PATIENTUSERS)
            .document(patientId)
            .collection(Constants.EXERCISE)

        exerciseCollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                val exerciseList = querySnapshot.documents.mapNotNull { documentSnapshot ->
                    documentSnapshot.toObject(Exercise::class.java)
                }
                onComplete(exerciseList)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreClass", "Error retrieving exercises", e)
                onComplete(emptyList()) // Return an empty list in case of failure
            }
    }


    /**
     * A function to make an entry of the registered user in the Firestore database.
     */
    fun storeExerciseData(activity: CameraActivity, exerciseInfo: Exercise, exerciseName: String) {
        // Access the "Exercises" collection under the specific patient document
        val exerciseTypeCollectionRef = mFireStore.collection(Constants.PATIENTUSERS)
            .document(getCurrentUserID()) // Patient document
            .collection(exerciseName)

        // Retrieve the current document count to generate an incremented document name
        exerciseTypeCollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                val documentCount = querySnapshot.size() + 1 // Increment count for new document
                val newExerciseDocName = "$exerciseName$documentCount" // e.g., "ElbowExercise1"

                // Add the exercise details as a new document in the specific exercise collection
                exerciseTypeCollectionRef.document(newExerciseDocName)
                    .set(exerciseInfo, SetOptions.merge()) // Store exercise info
                    .addOnSuccessListener {
                        // Success logic, if needed
                        // activity.userRegisteredSuccess() // Uncomment if needed
                    }
                    .addOnFailureListener { e ->
                        Log.e(activity.javaClass.simpleName, "Error writing document", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error retrieving documents", e)
            }
    }

    /**
     * A function to SignIn using firebase and get the user details from Firestore Database.
     */
    fun loadUserDetails(activity: Activity) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.PATIENTUSERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInPatient = document.toObject(Patient::class.java)

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        if (loggedInPatient != null) {
                            activity.signInSuccess(loggedInPatient)
                        }
                    }
                    is MainActivity -> {
                        if (loggedInPatient != null) {
                            activity.updateNavigationUserDetails(loggedInPatient)
                            activity.updateAppbarTile(loggedInPatient)
                        }
                    }
                    is MyProfileActivity -> {
                        if (loggedInPatient != null) {
                            activity.setUserDataInUI(loggedInPatient)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    fun loadUserDoctorDetails(activity: Activity) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.DOCTORUSERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInDoctor = document.toObject(Doctor::class.java)

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is DoctorSignInActivity -> {
                        if (loggedInDoctor != null) {
                            activity.signInSuccessDoctor(loggedInDoctor)
                        }
                    }
                    is DoctorMainActivity -> {
                        if (loggedInDoctor != null) {
                            activity.updateNavigationUserDetails(loggedInDoctor)
                            activity.updateAppbarTile(loggedInDoctor)
                        }
                    }
                    is DoctorProfileActivity -> {
                        if (loggedInDoctor != null) {
                            activity.setUserDataInUI(loggedInDoctor)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is DoctorSignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is DoctorMainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is DoctorProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    /**
     * A function to update the user profile data into the database.
     */
    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.PATIENTUSERS) // Collection Name
            .document(getCurrentUserID()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                Log.e(activity.javaClass.simpleName, "Profile Data updated successfully!")

                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                // Notify the success result.
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
                Toast.makeText(activity, "Error while updating Profile", Toast.LENGTH_SHORT).show()

            }
    }

    /**
     * A function to update the user profile data into the database.
     */
    fun updateDoctorProfileData(activity: DoctorProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.DOCTORUSERS) // Collection Name
            .document(getCurrentUserID()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                Log.e(activity.javaClass.simpleName, "Doctor Profile Data updated successfully!")

                Toast.makeText(activity, "Doctor Profile updated successfully!", Toast.LENGTH_SHORT).show()

                // Notify the success result.
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
                Toast.makeText(activity, "Error while updating Profile", Toast.LENGTH_SHORT).show()

            }
    }

    /**
     * A function for getting the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun getUserType(userID: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // First, check in the "patient" collection
        val patientRef = db.collection("patient").document(userID)

        patientRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // If document exists in "patient", it's a patient
                callback("patient")
            } else {
                // If not found in "patient", check in "doctor" collection
                val doctorRef = db.collection("doctor").document(userID)

                doctorRef.get().addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        // If document exists in "doctor", it's a doctor
                        callback("doctor")
                    } else {
                        // If not found in both collections, return null
                        callback(null)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Firestore Error", "Error fetching doctor document", exception)
                    callback(null)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore Error", "Error fetching patient document", exception)
            callback(null)
        }
    }


}