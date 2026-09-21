package com.example.campushire

import com.example.campushire.data.Job
import com.example.campushire.util.JobFilter
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for the job data helpers used with the REST API results. */
class JobFilterTest {

    private val jobs = listOf(
        Job(id = "1", title = "Marketing Intern", jobType = "Internship", location = "Worldwide"),
        Job(id = "2", title = "Dev", jobType = "Full-time", location = "USA Only"),
        Job(id = "3", title = "Tutor", jobType = "Part-time", location = "Europe")
    )

    @Test fun formatJobType_mapsApiValues() {
        assertEquals("Full-time", JobFilter.formatJobType("full_time"))
        assertEquals("Part-time", JobFilter.formatJobType("part_time"))
        assertEquals("Other", JobFilter.formatJobType(null))
    }

    @Test fun filter_all_returnsEverything() = assertEquals(3, JobFilter.filter(jobs, "All", "").size)

    @Test fun filter_byWorkType() {
        val result = JobFilter.filter(jobs, "Internship", "")
        assertEquals(1, result.size)
        assertEquals("Marketing Intern", result.first().title)
    }

    @Test fun filter_byLocation_isCaseInsensitive() {
        assertEquals("2", JobFilter.filter(jobs, "All", "usa").single().id)
    }

    @Test fun filter_noMatch_returnsEmpty() = assertTrue(JobFilter.filter(jobs, "Freelance", "").isEmpty())

    @Test fun filter_byAvailabilityAndMinimumSalary() {
        val evening = Job(id = "4", availability = "Evenings", salary = "R8 000 per month")
        val weekend = Job(id = "5", availability = "Weekends", salary = "R2 000 per month")

        val result = JobFilter.filter(listOf(evening, weekend), "All", "", setOf("Evenings"), 5000)

        assertEquals(listOf(evening), result)
    }

    @Test fun salaryValue_handlesCurrencyFormatting() {
        assertEquals(8000, JobFilter.salaryValue("R8 000 per month"))
    }

    @Test fun stripHtml_removesTagsAndKeepsBullets() {
        val text = JobFilter.stripHtml("<p>Hello &amp; welcome</p><ul><li>Skill one</li></ul>")
        assertFalse(text.contains("<"))
        assertTrue(text.contains("Hello & welcome"))
        assertTrue(text.contains("• Skill one"))
    }
}

