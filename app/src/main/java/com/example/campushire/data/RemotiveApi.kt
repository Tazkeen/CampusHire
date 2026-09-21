package com.example.campushire.data

import com.example.campushire.util.JobFilter
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * REST API integration.
 * We consume the free public Remotive Jobs API: https://remotive.com/api/remote-jobs
 * Docs: https://github.com/remotive-com/remote-jobs-api  (attribution required, please link back to Remotive)
 */
data class JobsResponse(
    @SerializedName("jobs") val jobs: List<RemoteJob>?
)

data class RemoteJob(
    @SerializedName("id") val id: Long?,
    @SerializedName("url") val url: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("company_name") val companyName: String?,
    @SerializedName("company_logo") val companyLogo: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("job_type") val jobType: String?,
    @SerializedName("publication_date") val publicationDate: String?,
    @SerializedName("candidate_required_location") val location: String?,
    @SerializedName("salary") val salary: String?,
    @SerializedName("description") val description: String?
)

/** Maps the raw API object to our own clean [Job] model (null-safe). */
fun RemoteJob.toJob() = Job(
    id = (id ?: 0L).toString(),
    title = title.orEmpty(),
    company = companyName.orEmpty(),
    category = category.orEmpty(),
    jobType = JobFilter.formatJobType(jobType),
    location = location.orEmpty().ifBlank { "Remote" },
    salary = salary.orEmpty().ifBlank { "Salary not listed" },
    description = description.orEmpty(),
    url = url.orEmpty(),
    postedDate = publicationDate.orEmpty().take(10),
    logo = companyLogo.orEmpty()
)

interface RemotiveService {
    @GET("api/remote-jobs")
    suspend fun getJobs(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 50
    ): JobsResponse
}

object ApiClient {
    val service: RemotiveService by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        Retrofit.Builder()
            .baseUrl("https://remotive.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RemotiveService::class.java)
    }
}

