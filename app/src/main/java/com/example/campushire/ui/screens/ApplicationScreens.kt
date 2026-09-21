@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.campushire.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushire.AppViewModel
import com.example.campushire.data.Constants
import com.example.campushire.ui.*

private fun getFileName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (i >= 0 && c.moveToFirst()) return c.getString(i)
    }
    return uri.lastPathSegment ?: "CV"
}

/** Screen 7 - Apply for a job */
@Composable
fun ApplyScreen(vm: AppViewModel, id: String, onBack: () -> Unit, onDone: () -> Unit) {
    val job = vm.findJob(id)
    val context = LocalContext.current
    var cvName by remember { mutableStateOf("") }
    var cover by remember { mutableStateOf("") }
    var confirmed by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) cvName = getFileName(context, uri)
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Apply", onBack)
        if (job == null) { EmptyState("This job is no longer available."); return@Column }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Apply for", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(job.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CompanyLogo(job, vm.settings.dataSaver, 40.dp)
                    Spacer(Modifier.width(10.dp))
                    Column { Text(job.company, fontWeight = FontWeight.SemiBold); Text("${job.location} • ${job.jobType}", fontSize = 12.sp) }
                }
            }
            Text("1. Personal Information", fontWeight = FontWeight.Bold)
            InfoCard(listOf(vm.profile.name, vm.profile.email, vm.profile.phone))
            Text("2. Education", fontWeight = FontWeight.Bold)
            InfoCard(listOf(vm.profile.university, listOf(vm.profile.course, vm.profile.yearOfStudy).filter { it.isNotBlank() }.joinToString(" • ")))
            Text("3. Attach CV *", fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = { picker.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) },
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CloudUpload, null)
                Spacer(Modifier.width(8.dp))
                Text("Choose file (PDF, DOC or DOCX)")
            }
            if (cvName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(cvName, fontSize = 14.sp)
                }
            }
            Text("4. Cover Letter (Optional)", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = cover, onValueChange = { if (it.length <= 500) cover = it },
                placeholder = { Text("Write a short cover letter...") },
                supportingText = { Text("${cover.length} / 500") },
                minLines = 4, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(confirmed, { confirmed = it })
                Text("I confirm that the information provided is true and correct.", fontSize = 13.sp)
            }
        }
        Button(
            onClick = { vm.apply(job, cover, cvName, confirmed, onDone) },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp), shape = RoundedCornerShape(12.dp)
        ) { Text("Submit Application") }
    }
}

@Composable
private fun InfoCard(lines: List<String>) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            lines.filter { it.isNotBlank() }.forEachIndexed { i, l ->
                Text(l, fontWeight = if (i == 0) FontWeight.SemiBold else FontWeight.Normal, fontSize = 14.sp)
            }
            if (lines.none { it.isNotBlank() }) Text("Add these details in Profile > Edit", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Screen 8 - Application Tracker */
@Composable
fun ApplicationsScreen(vm: AppViewModel, onJob: (String) -> Unit) {
    val tabs = listOf("All", "Active", "Completed", "Withdrawn")
    var tab by remember { mutableIntStateOf(0) }
    val list = vm.applications.filter {
        when (tab) {
            0 -> true
            1 -> it.status in Constants.ACTIVE
            2 -> it.status in Constants.COMPLETED
            else -> it.status == "Withdrawn"
        }
    }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("My Applications")
        TabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t, fontSize = 13.sp) }) }
        }
        if (list.isEmpty()) {
            EmptyState("No applications here yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(list, key = { it.id }) { a ->
                    Card(
                        Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(a.jobTitle, fontWeight = FontWeight.Bold)
                                    Text(a.company, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge(a.status)
                            }
                            Text("${a.location} • ${a.jobType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Applied on ${formatDate(a.appliedAt)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (a.status in Constants.ACTIVE) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { onJob(a.id) }) { Text("View job") }
                                    // Demo helper: simulates the employer moving the application along
                                    TextButton(onClick = { vm.advanceStatus(a) }) { Text("Advance status (demo)") }
                                    TextButton(onClick = { vm.withdraw(a) }) { Text("Withdraw", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Saved jobs tab */
@Composable
fun SavedScreen(vm: AppViewModel, onJob: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Saved Jobs")
        if (vm.saved.isEmpty()) {
            EmptyState("You haven't saved any jobs yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.saved, key = { it.id }) { j ->
                    JobCard(j, true, vm.settings.dataSaver, false, { onJob(j.id) }, { vm.toggleSave(j) })
                }
            }
        }
    }
}

