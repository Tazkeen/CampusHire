@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.campushire.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushire.AppViewModel
import com.example.campushire.R
import com.example.campushire.data.Constants
import com.example.campushire.ui.AppTextField
import com.example.campushire.ui.DropdownField
import com.example.campushire.ui.ScreenHeader
import com.example.campushire.ui.theme.Blue
import com.example.campushire.ui.theme.Navy

/** Screen 1 - Welcome / Login */
@Composable
fun WelcomeScreen(vm: AppViewModel, onLoggedIn: () -> Unit, onRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val soon = { Toast.makeText(context, "Social sign-in arrives in the final PoE", Toast.LENGTH_SHORT).show() }

    Column(
        Modifier.fillMaxSize().background(Navy).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(R.drawable.campushire_logo),
            contentDescription = "CampusHire logo",
            modifier = Modifier.size(112.dp)
        )
        Row {
            Text("Campus", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("Hire", color = Color(0xFF4D8DFF), fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }
        Text("Student Jobs and Career Networking", color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        AppTextField(email, { email = it }, "Email", icon = Icons.Filled.Email, keyboardType = KeyboardType.Email, onDark = true)
        Spacer(Modifier.height(12.dp))
        AppTextField(password, { password = it }, "Password", icon = Icons.Filled.Lock, isPassword = true, onDark = true, imeAction = ImeAction.Done)
        Text(
            "Forgot password?", color = Color(0xFF7FB0FF), fontSize = 13.sp,
            modifier = Modifier.align(Alignment.End).padding(top = 8.dp).clickableNoRipple { vm.resetPassword(email) }
        )
        vm.authError?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color(0xFFFF8A8A), textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.login(email, password, onLoggedIn) },
            enabled = !vm.authLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            if (vm.authLoading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp) else Text("Login")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { vm.authError = null; onRegister() },
            modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) { Text("Register") }

        Spacer(Modifier.height(24.dp))
        Text("or continue with", color = Color(0xFFB8C4E6), fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Google", "Microsoft", "LinkedIn").forEach { name ->
                OutlinedButton(onClick = soon, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                    Text(name, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("By continuing, you agree to our Terms of Service and Privacy Policy.", color = Color(0xFFB8C4E6), fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

/** Screen 2 - Registration */
@Composable
fun RegisterScreen(vm: AppViewModel, onBack: () -> Unit, onRegistered: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var uni by remember { mutableStateOf(Constants.UNIVERSITIES.first()) }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("Create Account", onBack)
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Join CampusHire and unlock amazing opportunities.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            AppTextField(name, { name = it }, "Full Name", icon = Icons.Filled.Person)
            AppTextField(email, { email = it }, "Email", icon = Icons.Filled.Email, keyboardType = KeyboardType.Email)
            AppTextField(phone, { phone = it }, "Phone Number", icon = Icons.Filled.Phone, keyboardType = KeyboardType.Phone)
            DropdownField("University / Institution", uni, Constants.UNIVERSITIES, { uni = it }, Icons.Filled.School)
            AppTextField(password, { password = it }, "Password", icon = Icons.Filled.Lock, isPassword = true,
                supporting = "At least 8 characters with a number and a special character.")
            AppTextField(confirm, { confirm = it }, "Confirm Password", icon = Icons.Filled.Lock, isPassword = true, imeAction = ImeAction.Done)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(agreed, { agreed = it })
                Text("I agree to the Terms of Service and Privacy Policy.", fontSize = 13.sp)
            }
            vm.authError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { vm.register(name, email, phone, uni, password, confirm, agreed, onRegistered) },
                enabled = !vm.authLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)
            ) {
                if (vm.authLoading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp) else Text("Register")
            }
            TextButton(onClick = { vm.authError = null; onBack() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Already have an account? Login")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

