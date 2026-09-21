package com.example.campushire.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/** Firebase Authentication and Cloud Firestore data source. */
class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun uid(): String = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
    private fun userDoc() = db.collection("users").document(uid())

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun register(profile: UserProfile, password: String) {
        val result = auth.createUserWithEmailAndPassword(profile.email.trim(), password).await()
        val user = result.user ?: throw IllegalStateException("Account creation failed")
        profile.uid = user.uid
        db.collection("users").document(user.uid).set(profile).await()
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    fun logout() = auth.signOut()
    suspend fun sendPasswordReset(email: String) = auth.sendPasswordResetEmail(email.trim()).await()

    suspend fun changePassword(current: String, newPassword: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Not logged in")
        val email = user.email ?: throw IllegalStateException("No email on account")
        user.reauthenticate(EmailAuthProvider.getCredential(email, current)).await()
        user.updatePassword(newPassword).await()
    }

    suspend fun loadProfile(): UserProfile? = userDoc().get().await().toObject(UserProfile::class.java)
    suspend fun saveProfile(profile: UserProfile) { profile.uid = uid(); userDoc().set(profile).await() }
    suspend fun getSaved(): List<Job> = userDoc().collection("saved").get().await().toObjects(Job::class.java)
    suspend fun saveJob(job: Job) { userDoc().collection("saved").document(job.id).set(job).await() }
    suspend fun unsaveJob(id: String) { userDoc().collection("saved").document(id).delete().await() }
    suspend fun getApplications(): List<JobApplication> = userDoc().collection("applications")
        .orderBy("appliedAt", Query.Direction.DESCENDING).get().await().toObjects(JobApplication::class.java)
    suspend fun saveApplication(application: JobApplication) {
        userDoc().collection("applications").document(application.id).set(application).await()
    }
    suspend fun updateApplicationStatus(id: String, status: String) {
        userDoc().collection("applications").document(id).update("status", status).await()
    }
    suspend fun getNotifications(): List<AppNotification> = userDoc().collection("notifications")
        .orderBy("createdAt", Query.Direction.DESCENDING).get().await().documents.mapNotNull { document ->
            document.toObject(AppNotification::class.java)?.also { it.id = document.id }
        }
    suspend fun addNotification(notification: AppNotification) {
        userDoc().collection("notifications").add(notification).await()
    }
}

