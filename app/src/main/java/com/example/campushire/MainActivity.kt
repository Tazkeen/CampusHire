package com.example.campushire

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campushire.ui.screens.*
import com.example.campushire.ui.LocalCampusHireLanguage
import com.example.campushire.ui.theme.CampusHireTheme
import com.example.campushire.ui.theme.Navy

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            CampusHireTheme(
                darkTheme = vm.settings.darkMode,
                highContrast = vm.settings.highContrast,
                fontScale = vm.settings.fontScale
            ) {
                CompositionLocalProvider(LocalCampusHireLanguage provides vm.settings.language) {
                    AppRoot(vm)
                }
            }
        }
    }
}

private val bottomRoutes = listOf("home", "jobs", "applications", "saved", "profile")

@Composable
fun AppRoot(vm: AppViewModel) {
    val nav = rememberNavController()
    val context = LocalContext.current
    val start = remember { if (vm.isLoggedIn) "home" else "welcome" }
    val route = nav.currentBackStackEntryAsState().value?.destination?.route

    // Show one-shot messages (errors / confirmations) from the ViewModel as Toasts
    LaunchedEffect(vm.message) {
        vm.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            vm.clearMessage()
        }
    }

    Scaffold(
        containerColor = if (route == "welcome") Navy else MaterialTheme.colorScheme.background,
        bottomBar = { if (route in bottomRoutes) BottomBar(route, nav, vm.settings.language) }
    ) { padding ->
        NavHost(navController = nav, startDestination = start, modifier = Modifier.padding(padding)) {

            composable("welcome") {
                WelcomeScreen(
                    vm,
                    onLoggedIn = { nav.navigate("home") { popUpTo("welcome") { inclusive = true } } },
                    onRegister = { nav.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onRegistered = { nav.navigate("home") { popUpTo("welcome") { inclusive = true } } }
                )
            }
            composable("home") { HomeScreen(vm, onJob = { nav.navigate("detail/$it") }, onNav = { nav.navigate(it) }) }
            composable("jobs") { JobsScreen(vm, onJob = { nav.navigate("detail/$it") }, onFilters = { nav.navigate("filters") }) }
            composable("filters") { FiltersScreen(vm, onBack = { nav.popBackStack() }) }
            composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                JobDetailScreen(vm, id, onBack = { nav.popBackStack() }, onApply = { nav.navigate("apply/$id") })
            }
            composable("apply/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                ApplyScreen(vm, id, onBack = { nav.popBackStack() }, onDone = {
                    nav.navigate("applications") { popUpTo("home") }
                })
            }
            composable("applications") { ApplicationsScreen(vm, onJob = { nav.navigate("detail/$it") }) }
            composable("saved") { SavedScreen(vm, onJob = { nav.navigate("detail/$it") }) }
            composable("profile") {
                ProfileScreen(vm, onEdit = { nav.navigate("editProfile") }, onSettings = { nav.navigate("settings") }, onNav = { nav.navigate(it) })
            }
            composable("editProfile") { EditProfileScreen(vm, onBack = { nav.popBackStack() }) }
            composable("settings") {
                SettingsScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onEditProfile = { nav.navigate("editProfile") },
                    onLogout = {
                        vm.logout()
                        nav.navigate("welcome") { popUpTo(nav.graph.id) { inclusive = true } }
                    }
                )
            }
            composable("resources") { ResourcesScreen(onBack = { nav.popBackStack() }) }
            composable("notifications") { NotificationsScreen(vm, onBack = { nav.popBackStack() }) }
        }
    }
}

@Composable
private fun BottomBar(current: String?, nav: NavHostController, language: String) {
    val items = listOf(
        Triple("home", "Home", Icons.Outlined.Home),
        Triple("jobs", "Jobs", Icons.Outlined.Work),
        Triple("applications", "Applications", Icons.Outlined.Description),
        Triple("saved", "Saved", Icons.Outlined.BookmarkBorder),
        Triple("profile", "Profile", Icons.Outlined.Person)
    )
    NavigationBar {
        items.forEach { (r, englishLabel, icon) ->
            val label = navigationLabel(r, englishLabel, language)
            NavigationBarItem(
                selected = current == r,
                onClick = {
                    nav.navigate(r) {
                        // Keep one stable copy of the main destinations. Do not restore
                        // a previous tab state: Home must always open the Home screen.
                        popUpTo(nav.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, maxLines = 1, fontSize = 10.sp) }
            )
        }
    }
}

private fun navigationLabel(route: String, english: String, language: String): String = when (language) {
    "Afrikaans" -> mapOf("home" to "Tuis", "jobs" to "Werk", "applications" to "Aansoeke", "saved" to "Gestoor", "profile" to "Profiel")[route] ?: english
    "isiZulu" -> mapOf("home" to "Ekhaya", "jobs" to "Imisebenzi", "applications" to "Izicelo", "saved" to "Kulondoloziwe", "profile" to "Iphrofayela")[route] ?: english
    else -> english
}

