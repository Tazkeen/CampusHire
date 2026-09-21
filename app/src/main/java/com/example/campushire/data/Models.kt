package com.example.campushire.data

/**
 * Data models used across CampusHire.
 * All fields are `var` with defaults so Firebase Firestore can (de)serialise them automatically.
 */
data class Job(
    var id: String = "",
    var title: String = "",
    var company: String = "",
    var category: String = "",
    var jobType: String = "",
    var location: String = "",
    var salary: String = "",
    var description: String = "",
    var url: String = "",
    var postedDate: String = "",
    var logo: String = "",
    /** Employer verification is explicitly shown to help students assess trust. */
    var isVerified: Boolean = false,
    /** Student-friendly availability such as Evenings, Weekends, or Flexible. */
    var availability: String = "Flexible"
)

data class UserProfile(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var university: String = "",
    var course: String = "",
    var yearOfStudy: String = "",
    var location: String = "",
    var skills: String = "",          // comma separated, e.g. "Excel, Python"
    var availability: String = ""
)

data class JobApplication(
    var id: String = "",              // same as the job id
    var jobTitle: String = "",
    var company: String = "",
    var jobType: String = "",
    var location: String = "",
    var coverLetter: String = "",
    var cvName: String = "",
    var status: String = "Applied",
    var appliedAt: Long = 0L
)

data class AppNotification(
    var id: String = "",
    var title: String = "",
    var message: String = "",
    var createdAt: Long = 0L
)

object Constants {
    val STATUS_FLOW = listOf("Applied", "Under Review", "Interview", "Successful")
    val ACTIVE = listOf("Applied", "Under Review", "Interview")
    val COMPLETED = listOf("Successful", "Not Selected")

    /** Remotive category slug to display label. "All" means no category filter. */
    val CATEGORIES = listOf(
        "All" to "All categories",
        "software-dev" to "Software Development",
        "marketing" to "Marketing",
        "design" to "Design",
        "customer-support" to "Customer Support",
        "data" to "Data",
        "writing" to "Writing",
        "sales" to "Sales",
        "business" to "Business"
    )
    val WORK_TYPES = listOf("All", "Full-time", "Part-time", "Internship", "Freelance", "Contract")
    val UNIVERSITIES = listOf(
        "University of Johannesburg", "University of Cape Town", "University of the Witwatersrand",
        "Stellenbosch University", "University of Pretoria", "UKZN", "IIE Varsity College",
        "Rosebank International College", "Other"
    )
}

