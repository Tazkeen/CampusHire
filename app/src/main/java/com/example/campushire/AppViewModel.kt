package com.example.campushire

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushire.data.*
import com.example.campushire.util.Validators
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch

private const val TAG = "CampusHireVM"

/**
 * Single ViewModel shared by every screen. It owns the UI state and talks to the
 * REST API (Retrofit) and Firebase.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FirebaseRepository()
    val settings = SettingsStore(app)

    // ---- Auth state ----
    var isLoggedIn by mutableStateOf(repo.isLoggedIn())
        private set
    var authLoading by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)

    /** One-shot message shown as a Toast by the root composable. */
    var message by mutableStateOf<String?>(null)

    // ---- User data (Firestore) ----
    var profile by mutableStateOf(UserProfile())
        private set
    var saved by mutableStateOf<List<Job>>(emptyList())
        private set
    var applications by mutableStateOf<List<JobApplication>>(emptyList())
        private set
    var notifications by mutableStateOf<List<AppNotification>>(emptyList())
        private set

    // ---- Jobs (REST API) ----
    var jobs by mutableStateOf<List<Job>>(emptyList())
        private set
    var jobsLoading by mutableStateOf(false)
        private set
    var jobsError by mutableStateOf<String?>(null)
        private set
    var searchQuery by mutableStateOf("")
    var workType by mutableStateOf("All")
    var category by mutableStateOf("All")
    var locationFilter by mutableStateOf("")
    var availabilityFilters by mutableStateOf<Set<String>>(emptySet())
    var minimumSalary by mutableStateOf<Int?>(null)

    init {
        Log.d(TAG, "ViewModel created. Logged in = $isLoggedIn")
        if (isLoggedIn) refreshUserData()
        loadJobs()
    }

    fun clearMessage() { message = null }

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        authError = null
        if (!Validators.isValidEmail(email)) { authError = "Enter a valid email address"; return }
        if (password.isEmpty()) { authError = "Enter your password"; return }
        viewModelScope.launch {
            authLoading = true
            try {
                repo.login(email, password)
                loadUserDataNow()
                isLoggedIn = true
                Log.d(TAG, "Login success")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                authError = friendly(e)
            } finally {
                authLoading = false
            }
        }
    }

    fun register(
        name: String, email: String, phone: String, university: String,
        password: String, confirm: String, agreed: Boolean, onSuccess: () -> Unit
    ) {
        authError = null
        Validators.validateRegistration(name, email, phone, password, confirm, agreed)?.let {
            authError = it
            return
        }
        viewModelScope.launch {
            authLoading = true
            try {
                val p = UserProfile(name = name.trim(), email = email.trim(), phone = phone.trim(), university = university)
                repo.register(p, password)
                profile = p
                saved = emptyList(); applications = emptyList(); notifications = emptyList()
                isLoggedIn = true
                Log.d(TAG, "Registration success for ${p.email}")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Registration failed", e)
                authError = friendly(e)
            } finally {
                authLoading = false
            }
        }
    }

    fun logout() {
        repo.logout()
        isLoggedIn = false
        profile = UserProfile(); saved = emptyList(); applications = emptyList(); notifications = emptyList()
        Log.d(TAG, "User logged out")
    }

    fun resetPassword(email: String) {
        if (!Validators.isValidEmail(email)) { message = "Type your email above first"; return }
        viewModelScope.launch {
            try {
                repo.sendPasswordReset(email)
                message = "Password reset email sent"
            } catch (e: Exception) {
                Log.e(TAG, "Reset failed", e)
                message = friendly(e)
            }
        }
    }

    fun changePassword(current: String, newPassword: String, onDone: () -> Unit) {
        if (current.isEmpty()) { message = "Enter your current password"; return }
        if (!Validators.isValidPassword(newPassword)) {
            message = "New password needs 8+ characters, a number and a special character"; return
        }
        viewModelScope.launch {
            try {
                repo.changePassword(current, newPassword)
                message = "Password updated"
                onDone()
            } catch (e: Exception) {
                Log.e(TAG, "Change password failed", e)
                message = friendly(e)
            }
        }
    }

    // ------------------------------------------------------------------
    // Jobs (REST API)
    // ------------------------------------------------------------------
    fun loadJobs() {
        viewModelScope.launch {
            jobsLoading = true
            jobsError = null
            try {
                val cat = if (category == "All") null else category
                val response = ApiClient.service.getJobs(search = searchQuery.trim().ifBlank { null }, category = cat)
                jobs = response.jobs.orEmpty().map { it.toJob() }
                Log.d(TAG, "Loaded ${jobs.size} jobs from Remotive (search='$searchQuery', category=$category)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load jobs", e)
                jobs = demoJobs
                jobsError = "Showing sample opportunities while live jobs are unavailable."
            } finally {
                jobsLoading = false
            }
        }
    }

    fun findJob(id: String): Job? =
        jobs.firstOrNull { it.id == id } ?: saved.firstOrNull { it.id == id }

    fun clearFilters() {
        workType = "All"; category = "All"; locationFilter = ""
        availabilityFilters = emptySet(); minimumSalary = null
    }

    fun toggleAvailability(value: String) {
        availabilityFilters = if (value in availabilityFilters) availabilityFilters - value else availabilityFilters + value
    }

    // ------------------------------------------------------------------
    // Saved jobs
    // ------------------------------------------------------------------
    fun isSaved(id: String) = saved.any { it.id == id }

    fun hasApplied(id: String) = applications.any { it.id == id }

    fun toggleSave(job: Job) {
        viewModelScope.launch {
            try {
                if (isSaved(job.id)) {
                    repo.unsaveJob(job.id)
                    saved = saved.filterNot { it.id == job.id }
                    message = "Removed from saved jobs"
                } else {
                    repo.saveJob(job)
                    saved = saved + job
                    message = "Job saved"
                }
                Log.d(TAG, "Saved jobs now: ${saved.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Toggle save failed", e)
                message = friendly(e)
            }
        }
    }

    // ------------------------------------------------------------------
    // Applications
    // ------------------------------------------------------------------
    fun apply(job: Job, cover: String, cvName: String, confirmed: Boolean, onSuccess: () -> Unit) {
        if (cvName.isBlank()) { message = "Please attach your CV"; return }
        if (!confirmed) { message = "Please confirm your information is correct"; return }
        if (hasApplied(job.id)) { message = "You already applied for this job"; return }
        viewModelScope.launch {
            try {
                val a = JobApplication(
                    id = job.id, jobTitle = job.title, company = job.company, jobType = job.jobType,
                    location = job.location, coverLetter = cover.trim(), cvName = cvName,
                    status = "Applied", appliedAt = System.currentTimeMillis()
                )
                repo.saveApplication(a)
                applications = listOf(a) + applications
                addNotification("Application received", "We've received your application for ${job.title} at ${job.company}.")
                message = "Application submitted!"
                Log.d(TAG, "Applied for ${job.title}")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Apply failed", e)
                message = friendly(e)
            }
        }
    }

    /** Demo helper: moves an application to its next stage (Applied -> Under Review -> Interview -> Successful). */
    fun advanceStatus(a: JobApplication) {
        val idx = Constants.STATUS_FLOW.indexOf(a.status)
        if (idx < 0 || idx >= Constants.STATUS_FLOW.lastIndex) return
        updateStatus(a, Constants.STATUS_FLOW[idx + 1])
    }

    fun withdraw(a: JobApplication) = updateStatus(a, "Withdrawn")

    private fun updateStatus(a: JobApplication, status: String) {
        viewModelScope.launch {
            try {
                repo.updateApplicationStatus(a.id, status)
                applications = applications.map { if (it.id == a.id) it.copy(status = status) else it }
                addNotification("Application update", "Your application for ${a.jobTitle} at ${a.company} is now: $status")
                Log.d(TAG, "Status of ${a.jobTitle} -> $status")
            } catch (e: Exception) {
                Log.e(TAG, "Status update failed", e)
                message = friendly(e)
            }
        }
    }

    // ------------------------------------------------------------------
    // Profile
    // ------------------------------------------------------------------
    fun saveProfile(p: UserProfile, onDone: () -> Unit) {
        if (!Validators.isValidName(p.name)) { message = "Please enter your name"; return }
        if (p.phone.isNotBlank() && !Validators.isValidPhone(p.phone)) { message = "Enter a valid phone number"; return }
        viewModelScope.launch {
            try {
                repo.saveProfile(p)
                profile = p
                message = "Profile updated"
                onDone()
            } catch (e: Exception) {
                Log.e(TAG, "Save profile failed", e)
                message = friendly(e)
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    fun refreshUserData() {
        viewModelScope.launch { loadUserDataNow() }
    }

    private suspend fun loadUserDataNow() {
        try { repo.loadProfile()?.let { profile = it } } catch (e: Exception) { Log.e(TAG, "Load profile failed", e) }
        try { saved = repo.getSaved() } catch (e: Exception) { Log.e(TAG, "Load saved failed", e) }
        try { applications = repo.getApplications() } catch (e: Exception) { Log.e(TAG, "Load applications failed", e) }
        try { notifications = repo.getNotifications() } catch (e: Exception) { Log.e(TAG, "Load notifications failed", e) }
        Log.d(TAG, "User data loaded: ${saved.size} saved, ${applications.size} applications")
    }

    private suspend fun addNotification(title: String, msg: String) {
        if (!settings.notifications) return
        try {
            val n = AppNotification(title = title, message = msg, createdAt = System.currentTimeMillis())
            repo.addNotification(n)
            notifications = listOf(n) + notifications
        } catch (e: Exception) {
            Log.e(TAG, "Add notification failed", e)
        }
    }

    /** Converts Firebase errors into messages a student can understand. */
    private fun friendly(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "That password is too weak"
        is FirebaseAuthInvalidUserException -> "No account found with that email"
        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password"
        is FirebaseAuthUserCollisionException -> "An account with this email already exists"
        is FirebaseNetworkException -> "No internet connection"
        else -> e.localizedMessage ?: "Something went wrong"
    }
}

