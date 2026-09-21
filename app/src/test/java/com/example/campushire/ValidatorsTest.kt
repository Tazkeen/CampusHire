package com.example.campushire

import com.example.campushire.util.Validators
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for registration / login input validation. Run by GitHub Actions on every push. */
class ValidatorsTest {

    @Test fun validEmail_isAccepted() = assertTrue(Validators.isValidEmail("liam.naidoo@student.ac.za"))

    @Test fun emailWithoutAt_isRejected() = assertFalse(Validators.isValidEmail("liam.naidoo.student.ac.za"))

    @Test fun emptyEmail_isRejected() = assertFalse(Validators.isValidEmail(""))

    @Test fun strongPassword_isAccepted() = assertTrue(Validators.isValidPassword("Campus#2026"))

    @Test fun shortPassword_isRejected() = assertFalse(Validators.isValidPassword("a1!"))

    @Test fun passwordWithoutNumber_isRejected() = assertFalse(Validators.isValidPassword("Password!!"))

    @Test fun passwordWithoutSpecialChar_isRejected() = assertFalse(Validators.isValidPassword("Password123"))

    @Test fun southAfricanPhone_isAccepted() = assertTrue(Validators.isValidPhone("081 234 5678"))

    @Test fun tooShortPhone_isRejected() = assertFalse(Validators.isValidPhone("123"))

    @Test fun registration_passwordMismatch_returnsError() {
        val err = Validators.validateRegistration("Liam", "liam@x.co.za", "0812345678", "Campus#2026", "Different#1", true)
        assertEquals("Passwords do not match", err)
    }

    @Test fun registration_termsNotAccepted_returnsError() {
        val err = Validators.validateRegistration("Liam", "liam@x.co.za", "0812345678", "Campus#2026", "Campus#2026", false)
        assertNotNull(err)
    }

    @Test fun registration_allValid_returnsNull() {
        val err = Validators.validateRegistration("Liam Naidoo", "liam@x.co.za", "0812345678", "Campus#2026", "Campus#2026", true)
        assertNull(err)
    }
}

