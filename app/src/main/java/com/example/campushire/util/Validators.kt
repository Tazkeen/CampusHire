package com.example.campushire.util

/** Pure Kotlin input validation, kept free of Android classes so it can be unit tested. */
object Validators {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String) = EMAIL.matches(email.trim())

    /** At least 8 characters, one digit and one special character (matches the Register screen hint). */
    fun isValidPassword(p: String) =
        p.length >= 8 && p.any { it.isDigit() } && p.any { !it.isLetterOrDigit() }

    fun isValidPhone(phone: String): Boolean = phone.filter { it.isDigit() }.length in 9..12

    fun isValidName(name: String) = name.trim().length >= 2

    /** @return an error message, or null when everything is valid. */
    fun validateRegistration(
        name: String, email: String, phone: String,
        password: String, confirm: String, agreed: Boolean
    ): String? = when {
        !isValidName(name) -> "Please enter your full name"
        !isValidEmail(email) -> "Enter a valid email address"
        !isValidPhone(phone) -> "Enter a valid phone number"
        !isValidPassword(password) -> "Password needs 8+ characters, a number and a special character"
        password != confirm -> "Passwords do not match"
        !agreed -> "You must agree to the Terms of Service and Privacy Policy"
        else -> null
    }
}

