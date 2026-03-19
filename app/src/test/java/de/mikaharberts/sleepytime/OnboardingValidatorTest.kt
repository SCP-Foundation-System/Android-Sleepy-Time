package de.mikaharberts.sleepytime

import de.mikaharberts.sleepytime.feature.onboarding.OnboardingValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingValidatorTest {

    @Test
    fun `name is valid when trimmed text is present and within limit`() {
        assertTrue(OnboardingValidator.isNameValid("  Mika  "))
    }

    @Test
    fun `name is invalid when empty after trimming`() {
        assertFalse(OnboardingValidator.isNameValid("   "))
    }

    @Test
    fun `sanitize name trims and limits length`() {
        val source = "  TraumtagebuchMitSehrLangemNamen  "
        assertEquals(25, OnboardingValidator.sanitizeName(source).length)
    }
}
