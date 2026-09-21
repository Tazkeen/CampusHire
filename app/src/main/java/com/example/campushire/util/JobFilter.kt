package com.example.campushire.util

import com.example.campushire.data.Job

/** Pure Kotlin helpers for job data (unit tested). */
object JobFilter {

    /** Converts the API's job_type value (e.g. "full_time") into a friendly label. */
    fun formatJobType(raw: String?): String = when (raw?.lowercase()) {
        "full_time" -> "Full-time"
        "part_time" -> "Part-time"
        "contract" -> "Contract"
        "freelance" -> "Freelance"
        "internship" -> "Internship"
        else -> "Other"
    }

    /** Client-side filtering used alongside the REST API search. "All"/blank means no filter. */
    fun filter(
        jobs: List<Job>,
        workType: String,
        location: String,
        availability: Set<String> = emptySet(),
        minimumSalary: Int? = null
    ): List<Job> =
        jobs.filter { job ->
            (workType == "All" || job.jobType == workType) &&
                (location.isBlank() || job.location.contains(location.trim(), ignoreCase = true)) &&
                (availability.isEmpty() || job.availability in availability || job.availability == "Flexible") &&
                (minimumSalary == null || salaryValue(job.salary) >= minimumSalary)
        }

    /** Extracts the first advertised amount for the prototype's salary-range filter. */
    fun salaryValue(salary: String): Int = Regex("\\d[\\d, ]*").find(salary)
        ?.value?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0

    /** Turns the API's HTML job description into readable plain text. */
    fun stripHtml(html: String): String = html
        .replace(Regex("(?i)<br\\s*/?>|</p>|</div>|</h\\d>"), "\n")
        .replace(Regex("(?i)<li[^>]*>"), "\n• ")
        .replace(Regex("<[^>]*>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}

