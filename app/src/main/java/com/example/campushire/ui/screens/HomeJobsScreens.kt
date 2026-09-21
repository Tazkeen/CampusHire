@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.campushire.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushire.AppViewModel
import com.example.campushire.data.Constants
import com.example.campushire.ui.*
import com.example.campushire.util.JobFilter

/** Screen 3 - Home */
@Composable
fun HomeScreen(vm: AppViewModel, onJob: (String) -> Unit, onNav: (String) -> Unit) {
    val firstName = vm.profile.name.substringBefore(" ").ifBlank { "there" }
    val ds = vm.settings.dataSaver
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Hi, $firstName! 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Let's find your next opportunity.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                IconButton(onClick = { onNav("notifications") }) { Icon(Icons.Filled.Notifications, "Notifications") }
            }
        }
        item { SectionTitle("Recommended for you", "View all") { onNav("jobs") } }
        if (vm.jobsLoading) item { LoadingBox() }
        vm.jobsError?.let { err -> item { Text(err, color = MaterialTheme.colorScheme.error) } }
        vm.jobs.firstOrNull()?.let { j ->
            item { JobCard(j, vm.isSaved(j.id), ds, true, { onJob(j.id) }, { vm.toggleSave(j) }) }
        }
        item { SectionTitle("Recent Opportunities", "View all") { onNav("jobs") } }
        items(vm.jobs.drop(1).take(3)) { j ->
            JobCard(j, vm.isSaved(j.id), ds, false, { onJob(j.id) }, { vm.toggleSave(j) })
        }
        item { SectionTitle("Saved Jobs", "View all") { onNav("saved") } }
        val s = vm.saved.firstOrNull()
        if (s == null) item { Text("Nothing saved yet - tap the bookmark on any job.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
        else item { JobCard(s, true, ds, false, { onJob(s.id) }, { vm.toggleSave(s) }) }
        item { SectionTitle("Notifications", "View all") { onNav("notifications") } }
        val n = vm.notifications.firstOrNull()
        item {
            if (n == null) Text("No notifications yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            else Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(n.title, fontWeight = FontWeight.SemiBold)
                    Text(n.message, fontSize = 13.sp)
                }
            }
        }
    }
}

/** Screen 4 - Jobs (data comes from the REST API) */
@Composable
fun JobsScreen(vm: AppViewModel, onJob: (String) -> Unit, onFilters: () -> Unit) {
    val visible = JobFilter.filter(vm.jobs, vm.workType, vm.locationFilter, vm.availabilityFilters, vm.minimumSalary)
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Jobs", actions = {
            IconButton(onClick = onFilters) { Icon(Icons.Filled.FilterList, "Filters", tint = MaterialTheme.colorScheme.primary) }
        })
        OutlinedTextField(
            value = vm.searchQuery, onValueChange = { vm.searchQuery = it },
            placeholder = { Text("Search jobs by title, company or skill") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true, shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { vm.loadJobs() }),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Text("${visible.size} opportunities found", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        when {
            vm.jobsLoading -> LoadingBox()
            vm.jobsError != null -> Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(vm.jobsError.orEmpty(), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { vm.loadJobs() }) { Text("Retry") }
            }
            visible.isEmpty() -> EmptyState("No jobs match your search or filters.")
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visible, key = { it.id }) { j ->
                    JobCard(j, vm.isSaved(j.id), vm.settings.dataSaver, false, { onJob(j.id) }, { vm.toggleSave(j) })
                }
                item { Text("Jobs provided by Remotive (remotive.com)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

/** Screen 5 - Filters */
@Composable
fun FiltersScreen(vm: AppViewModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("Filters", onBack, actions = {
            TextButton(onClick = { vm.clearFilters() }) { Text("Clear all") }
        })
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Location", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = vm.locationFilter, onValueChange = { vm.locationFilter = it },
                placeholder = { Text("e.g. Worldwide, USA, Europe") }, singleLine = true,
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            )
            Text("Work Type", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Constants.WORK_TYPES.forEach { t ->
                    FilterChip(selected = vm.workType == t, onClick = { vm.workType = t }, label = { Text(t) })
                }
            }
            Text("Category", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Constants.CATEGORIES.forEach { (slug, label) ->
                    FilterChip(selected = vm.category == slug, onClick = { vm.category = slug }, label = { Text(label) })
                }
            }
            Text("Availability", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Weekdays", "Evenings", "Weekends", "Flexible").forEach { option ->
                    FilterChip(
                        selected = option in vm.availabilityFilters,
                        onClick = { vm.toggleAvailability(option) },
                        label = { Text(option) }
                    )
                }
            }
            Text("Minimum pay", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "Any", 2500 to "R2 500+", 5000 to "R5 000+", 8000 to "R8 000+").forEach { (value, label) ->
                    FilterChip(selected = vm.minimumSalary == value, onClick = { vm.minimumSalary = value }, label = { Text(label) })
                }
            }
            Button(
                onClick = { vm.loadJobs(); onBack() },
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)
            ) { Text("Apply Filters") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Screen 6 - Job Details */
@Composable
fun JobDetailScreen(vm: AppViewModel, id: String, onBack: () -> Unit, onApply: () -> Unit) {
    val job = vm.findJob(id)
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Job Details", onBack)
        if (job == null) { EmptyState("This job is no longer available."); return@Column }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            CompanyLogo(job, vm.settings.dataSaver, 64.dp)
            Spacer(Modifier.height(12.dp))
            Text(job.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(job.company, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (job.isVerified) {
                    Spacer(Modifier.width(6.dp))
                    AssistChip(onClick = {}, label = { Text("Verified employer", fontSize = 11.sp) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoChip(Icons.Outlined.LocationOn, job.location, Modifier.weight(1f, fill = false))
                InfoChip(Icons.Outlined.Schedule, job.jobType)
            }
            Spacer(Modifier.height(4.dp))
            InfoChip(Icons.Outlined.Business, "${job.salary}  •  Posted ${job.postedDate}")
            Text("Student availability: ${job.availability}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("Job Description", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(JobFilter.stripHtml(job.description).take(3500), fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            if (job.url.isNotBlank()) {
                TextButton(onClick = { uriHandler.openUri(job.url) }) { Text("View original posting on Remotive") }
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { vm.toggleSave(job) }, modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) { Text(if (vm.isSaved(job.id)) "Unsave Job" else "Save Job") }
            Button(
                onClick = onApply, enabled = !vm.hasApplied(job.id),
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)
            ) { Text(if (vm.hasApplied(job.id)) "Already Applied" else "Apply Now") }
        }
    }
}

/** Screen 11 - Notifications */
@Composable
fun NotificationsScreen(vm: AppViewModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Notifications", onBack)
        if (vm.notifications.isEmpty()) {
            EmptyState(if (vm.settings.notifications) "No notifications yet. Apply for a job to get updates." else "Notifications are switched off in Settings.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.notifications) { n ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(n.title, fontWeight = FontWeight.SemiBold)
                            Text(n.message, fontSize = 13.sp)
                            Text(
                                DateUtils.getRelativeTimeSpanString(n.createdAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString(),
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

