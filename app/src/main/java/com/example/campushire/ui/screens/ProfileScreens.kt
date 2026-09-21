@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.campushire.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushire.AppViewModel
import com.example.campushire.data.Constants
import com.example.campushire.ui.*

/** Screen 9 - Profile */
@Composable
fun ProfileScreen(vm: AppViewModel, onEdit: () -> Unit, onSettings: () -> Unit, onNav: (String) -> Unit) {
    val p = vm.profile
    val skills = p.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val completedFields = listOf(p.name, p.email, p.phone, p.university, p.course, p.yearOfStudy, p.location, p.skills, p.availability).count { it.isNotBlank() }
    val completion = (completedFields * 100 / 9).coerceAtMost(100)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("My Profile", actions = {
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Edit profile") }
            IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, "Settings") }
        })
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCircle(p.name)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(p.name.ifBlank { "Your name" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(p.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(p.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (p.location.isNotBlank()) Text(p.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            ProfileSection("Education") {
                Text(p.university.ifBlank { "Not set" }, fontWeight = FontWeight.SemiBold)
                val line = listOf(p.course, p.yearOfStudy).filter { it.isNotBlank() }.joinToString(" • ")
                if (line.isNotBlank()) Text(line, fontSize = 13.sp)
            }
            ProfileSection("Skills") {
                if (skills.isEmpty()) Text("Add skills using Edit Profile", fontSize = 13.sp)
                else FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    skills.forEach { s -> AssistChip(onClick = {}, label = { Text(s) }) }
                }
            }
            ProfileSection("Availability") { Text(p.availability.ifBlank { "Not set" }) }
            ProfileSection("Profile completion") {
                Text("$completion% complete", fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(progress = completion / 100f, modifier = Modifier.fillMaxWidth())
                if (completion < 100) Text("Add your course, skills and availability to unlock the Profile Complete badge.", fontSize = 12.sp)
            }
            ProfileSection("Peer recommendations") {
                Text("CampusHire peers can endorse strengths before you have formal work experience.", fontSize = 13.sp)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Priya Moodley", fontWeight = FontWeight.SemiBold)
                        Text("“A dependable teammate who communicates clearly and delivers quality work.”", fontSize = 13.sp)
                    }
                }
            }
            ProfileSection("Badges") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (completion == 100) AssistChip(onClick = {}, label = { Text("Profile Complete") })
                    if (vm.applications.isNotEmpty()) AssistChip(onClick = {}, label = { Text("Early Applicant") })
                    if (vm.saved.isNotEmpty()) AssistChip(onClick = {}, label = { Text("Active Learner") })
                    if (completion < 100 && vm.applications.isEmpty() && vm.saved.isEmpty()) Text("Complete your profile, save jobs and apply to earn badges.", fontSize = 13.sp)
                }
            }
            ProfileSection("My Activity") {
                Text("${vm.applications.size} applications  •  ${vm.saved.size} saved jobs")
            }
            OutlinedButton(onClick = { onNav("resources") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Career Resources") }
            OutlinedButton(onClick = { onNav("notifications") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Notifications") }
            Button(onClick = onSettings, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Settings") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            content()
        }
    }
}

/** Edit profile (reached from the Profile and Settings screens) */
@Composable
fun EditProfileScreen(vm: AppViewModel, onBack: () -> Unit) {
    val p = vm.profile
    var name by remember { mutableStateOf(p.name) }
    var phone by remember { mutableStateOf(p.phone) }
    var uni by remember { mutableStateOf(p.university.ifBlank { Constants.UNIVERSITIES.first() }) }
    var course by remember { mutableStateOf(p.course) }
    var year by remember { mutableStateOf(p.yearOfStudy) }
    var location by remember { mutableStateOf(p.location) }
    var skills by remember { mutableStateOf(p.skills) }
    var availability by remember { mutableStateOf(p.availability) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("Edit Profile", onBack)
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(name, { name = it }, "Full Name", icon = Icons.Filled.Person)
            AppTextField(phone, { phone = it }, "Phone Number", icon = Icons.Filled.Phone, keyboardType = KeyboardType.Phone)
            DropdownField("University / Institution", uni, Constants.UNIVERSITIES, { uni = it }, Icons.Filled.School)
            AppTextField(course, { course = it }, "Course (e.g. BCom Information Systems)")
            AppTextField(year, { year = it }, "Year of study (e.g. 2nd Year)")
            AppTextField(location, { location = it }, "Location", icon = Icons.Filled.LocationOn)
            AppTextField(skills, { skills = it }, "Skills (comma separated)", supporting = "Example: Excel, Communication, Python")
            AppTextField(availability, { availability = it }, "Availability (e.g. Weekdays: Evenings)")
            Button(
                onClick = {
                    vm.saveProfile(
                        p.copy(name = name, phone = phone, university = uni, course = course, yearOfStudy = year,
                            location = location, skills = skills, availability = availability),
                        onBack
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)
            ) { Text("Save Changes") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Screen 10 - Resources (static content for the prototype) */
@Composable
fun ResourcesScreen(onBack: () -> Unit) {
    var selectedResource by remember { mutableStateOf<ResourceGuide?>(null) }
    val sections = listOf(
        "CV Resources" to listOf(
            ResourceGuide("How to write a standout CV", "Tips and templates to impress employers.", "University of Cape Town Careers Service", "https://www.uct.ac.za/students/careers"),
            ResourceGuide("CV Checklist", "Make sure your CV is complete.", "Harvard Resume and Cover Letter Guide", "https://careerservices.fas.harvard.edu/resources/create-a-strong-resume/")),
        "Interview Resources" to listOf(
            ResourceGuide("Interview Preparation Guide", "Common questions and how to answer.", "Indeed Career Guide", "https://www.indeed.com/career-advice/interviewing"),
            ResourceGuide("Mock Interview Tips", "Practice and build confidence.", "The Muse Interview Advice", "https://www.themuse.com/advice/interviewing")),
        "Work-Study Resources" to listOf(
            ResourceGuide("Balancing Study and Work", "Manage time and stay productive.", "University of Johannesburg Student Support", "https://www.uj.ac.za/students/"),
            ResourceGuide("Finding Remote Work", "Tips for finding flexible opportunities.", "Remotive Remote Work Guide", "https://remotive.com/remote-jobs"))
    )
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Resources", onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sections.forEach { (title, items) ->
                item { Text(localized(title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                items.forEach { guide ->
                    item {
                        Card(Modifier.fillMaxWidth().clickable { selectedResource = guide }, shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(localized(guide.title), fontWeight = FontWeight.SemiBold)
                                Text(guide.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
    selectedResource?.let { guide ->
        val uriHandler = LocalUriHandler.current
        AlertDialog(
            onDismissRequest = { selectedResource = null },
            title = { Text(localized(guide.title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${guide.description}\n\nRecommended source:")
                    OutlinedButton(onClick = { uriHandler.openUri(guide.url) }, modifier = Modifier.fillMaxWidth()) {
                        Text(guide.source)
                    }
                    Text("The link opens in your device browser.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = { TextButton(onClick = { selectedResource = null }) { Text("Done") } }
        )
    }
}

private data class ResourceGuide(
    val title: String,
    val description: String,
    val source: String,
    val url: String
)

