package com.example.campushire.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import com.example.campushire.data.Job
import com.example.campushire.ui.theme.Blue

/** Reusable UI building blocks so every screen shares the same look (consistent layout, fonts, colours). */

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(localized(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        actions()
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(localized(title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(localized(action), color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(3.dp))
        Text(text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StatusBadge(status: String) {
    val (fg, bg) = when (status) {
        "Applied" -> Color(0xFF0B5CFF) to Color(0xFFE6EEFF)
        "Under Review" -> Color(0xFFB45309) to Color(0xFFFFF3D6)
        "Interview" -> Color(0xFF6D28D9) to Color(0xFFEDE7FF)
        "Successful" -> Color(0xFF15803D) to Color(0xFFDDF7E6)
        "Not Selected" -> Color(0xFF6B7280) to Color(0xFFE5E7EB)
        else -> Color(0xFFB91C1C) to Color(0xFFFDE2E2)   // Withdrawn
    }
    Text(
        status, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LoadingBox() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    icon: ImageVector? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    onDark: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    supporting: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    var visible by remember { mutableStateOf(false) }
    val gray = Color(0xFF6B7280)
    val colors = if (onDark) {
        OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            focusedTextColor = Color(0xFF111827), unfocusedTextColor = Color(0xFF111827),
            focusedLabelColor = Blue, unfocusedLabelColor = gray,
            focusedLeadingIconColor = Blue, unfocusedLeadingIconColor = gray,
            focusedTrailingIconColor = gray, unfocusedTrailingIconColor = gray,
            focusedBorderColor = Blue, unfocusedBorderColor = Color(0xFFD1D5DB), cursorColor = Blue
        )
    } else {
        OutlinedTextFieldDefaults.colors()
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(localized(label)) },
        modifier = modifier,
        leadingIcon = if (icon != null) { { Icon(icon, null) } } else null,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "Show or hide password")
                }
            }
        } else null,
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType, imeAction = imeAction),
        singleLine = singleLine,
        minLines = minLines,
        supportingText = if (supporting != null) { { Text(supporting) } } else null,
        shape = RoundedCornerShape(12.dp),
        colors = colors
    )
}

@Composable
fun DropdownField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit, icon: ImageVector? = null) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, singleLine = true,
            label = { Text(label) },
            leadingIcon = if (icon != null) { { Icon(icon, null) } } else null,
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Box(Modifier.matchParentSize().clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { o ->
                DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(o); expanded = false })
            }
        }
    }
}

/** Company logo from the API. With Data Saver ON we skip the download and show a coloured initial instead. */
@Composable
fun CompanyLogo(job: Job, dataSaver: Boolean, size: Dp = 48.dp) {
    val palette = listOf(Color(0xFF4F46E5), Color(0xFF16A34A), Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF0EA5E9), Color(0xFF9333EA))
    val bg = palette[(job.company.hashCode() and 0x7fffffff) % palette.size]
    val showImage = !dataSaver && job.logo.isNotBlank()
    Box(
        Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(if (showImage) Color.White else bg),
        contentAlignment = Alignment.Center
    ) {
        if (showImage) {
            AsyncImage(model = job.logo, contentDescription = job.company, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp))
        } else {
            Text(job.company.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun JobCard(
    job: Job,
    saved: Boolean,
    dataSaver: Boolean,
    highlight: Boolean = false,
    onClick: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            CompanyLogo(job, dataSaver)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(job.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(job.company, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (job.isVerified) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, "Verified employer", tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoChip(Icons.Outlined.LocationOn, job.location, Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(10.dp))
                    InfoChip(Icons.Outlined.Schedule, job.jobType)
                }
                Spacer(Modifier.height(4.dp))
                Text(job.salary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onSave) {
                Icon(
                    if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (saved) "Unsave job" else "Save job",
                    tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AvatarCircle(name: String, size: Dp = 72.dp) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    Box(Modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
        Text(initials.ifBlank { "?" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value / 3).sp)
    }
}

