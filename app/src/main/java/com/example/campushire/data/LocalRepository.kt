package com.example.campushire.data

/**
 * Device-only data source used by the prototype. It keeps the complete app flow
 * usable before a Firebase project is connected. Replace this with the Firebase
 * implementation when production authentication and cloud sync are required.
 */
class LocalRepository {
    private var currentUser: UserProfile? = null
    private var password = ""
    private val savedJobs = mutableListOf<Job>()
    private val jobApplications = mutableListOf<JobApplication>()
    private val appNotifications = mutableListOf<AppNotification>()

    fun isLoggedIn() = currentUser != null

    suspend fun register(profile: UserProfile, newPassword: String) {
        currentUser = profile.copy(uid = "local-${System.currentTimeMillis()}")
        password = newPassword
    }

    suspend fun login(email: String, suppliedPassword: String) {
        val user = currentUser
        if (user == null || !user.email.equals(email.trim(), ignoreCase = true) || password != suppliedPassword) {
            throw IllegalArgumentException("Create an account first, then sign in with those details")
        }
    }

    fun logout() { currentUser = null; savedJobs.clear(); jobApplications.clear(); appNotifications.clear() }

    suspend fun sendPasswordReset(email: String) {
        if (currentUser?.email?.equals(email.trim(), ignoreCase = true) != true) {
            throw IllegalArgumentException("No local account found with that email")
        }
    }

    suspend fun changePassword(current: String, newPassword: String) {
        if (password != current) throw IllegalArgumentException("Incorrect current password")
        password = newPassword
    }

    suspend fun loadProfile() = currentUser?.copy()
    suspend fun saveProfile(profile: UserProfile) { currentUser = profile.copy(uid = currentUser?.uid.orEmpty()) }
    suspend fun getSaved() = savedJobs.map { it.copy() }
    suspend fun saveJob(job: Job) { if (savedJobs.none { it.id == job.id }) savedJobs += job.copy() }
    suspend fun unsaveJob(id: String) { savedJobs.removeAll { it.id == id } }
    suspend fun getApplications() = jobApplications.sortedByDescending { it.appliedAt }.map { it.copy() }
    suspend fun saveApplication(application: JobApplication) {
        jobApplications.removeAll { it.id == application.id }
        jobApplications += application.copy()
    }
    suspend fun updateApplicationStatus(id: String, status: String) {
        val index = jobApplications.indexOfFirst { it.id == id }
        if (index >= 0) jobApplications[index] = jobApplications[index].copy(status = status)
    }
    suspend fun getNotifications() = appNotifications.sortedByDescending { it.createdAt }.map { it.copy() }
    suspend fun addNotification(notification: AppNotification) {
        appNotifications += notification.copy(id = "note-${System.currentTimeMillis()}")
    }
}

