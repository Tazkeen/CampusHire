@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.campushire.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.campushire.AppViewModel
import com.example.campushire.ui.*

/** Screen 12 - Settings. Every toggle here is saved on the device and takes effect immediately. */
@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit, onEditProfile: () -> Unit, onLogout: () -> Unit) {
    val s = vm.settings
    var showPassword by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<Pair<String, String>?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("Settings", onBack)
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SettingGroup("Account") {
                SettingNav(Icons.Filled.Person, "Edit Profile", onEditProfile)
                SettingNav(Icons.Filled.Lock, "Change Password") { showPassword = true }
                SettingNav(Icons.Filled.Email, "Email Preferences") { info = "Email Preferences" to "Job alert emails will be configurable in the final PoE." }
            }
            SettingGroup("Preferences") {
                SettingDropdown(Icons.Filled.Language, "Language", s.language, listOf("English", "Afrikaans", "isiZulu")) { s.updateLanguage(it) }
                SettingSwitch(Icons.Filled.Notifications, "Notifications", s.notifications) { s.updateNotifications(it) }
                SettingSwitch(Icons.Filled.DarkMode, "Dark Mode", s.darkMode) { s.updateDarkMode(it) }
                SettingSwitch(Icons.Filled.DataSaverOn, "Data Saver (no company logos)", s.dataSaver) { s.updateDataSaver(it) }
            }
            SettingGroup("Accessibility") {
                SettingDropdown(Icons.Filled.TextFields, "Text Size", s.textSize, listOf("Small", "Medium", "Large")) { s.updateTextSize(it) }
                SettingSwitch(Icons.Filled.Contrast, "High Contrast", s.highContrast) { s.updateHighContrast(it) }
            }
            SettingGroup("Information") {
                SettingNav(Icons.Filled.Help, "Help & Support") { info = "Help & Support" to "Email support@campushire.example for help." }
                SettingNav(Icons.Filled.Description, "Terms of Service") { info = "Terms of Service" to "Prototype terms of service placeholder." }
                SettingNav(Icons.Filled.PrivacyTip, "Privacy Policy") { info = "Privacy Policy" to "Your password is handled by Firebase Authentication and never stored by the app." }
                SettingNav(Icons.Filled.Info, "About CampusHire") { info = "About CampusHire" to "CampusHire v1.0 (Part 2 prototype). Job data by Remotive." }
            }
            OutlinedButton(
                onClick = onLogout, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showPassword) {
        var current by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPassword = false },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(current, { current = it }, "Current password", isPassword = true)
                    AppTextField(newPass, { newPass = it }, "New password", isPassword = true, supporting = "8+ characters, a number and a special character")
                }
            },
            confirmButton = { TextButton(onClick = { vm.changePassword(current, newPass) { showPassword = false } }) { Text("Update") } },
            dismissButton = { TextButton(onClick = { showPassword = false }) { Text("Cancel") } }
        )
    }
    info?.let { (t, m) ->
        AlertDialog(
            onDismissRequest = { info = null }, title = { Text(t) }, text = { Text(m) },
            confirmButton = { TextButton(onClick = { info = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun SettingGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(localized(title), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 6.dp))
        Card(
            Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) { Column(content = content) }
    }
}

@Composable
private fun SettingNav(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(localized(title), Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingSwitch(icon: ImageVector, title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(localized(title), Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SettingDropdown(icon: ImageVector, title: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(Modifier.fillMaxWidth().clickable { expanded = true }.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(localized(title), Modifier.weight(1f))
            Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Filled.ArrowDropDown, null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { o -> DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(o); expanded = false }) }
        }
    }
}

