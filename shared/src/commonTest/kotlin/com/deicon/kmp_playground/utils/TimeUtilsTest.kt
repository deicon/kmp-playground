package com.deicon.kmp_playground.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeUtilsTest {

    @Test
    fun `parseTime should parse hours correctly`() {
        assertEquals(2.0, TimeUtils.parseTime("2h"))
        assertEquals(2.5, TimeUtils.parseTime("2.5h"))
        assertEquals(0.5, TimeUtils.parseTime("0.5h"))
        assertEquals(10.0, TimeUtils.parseTime("10h"))
    }

    @Test
    fun `parseTime should parse days correctly`() {
        assertEquals(8.0, TimeUtils.parseTime("1d"))
        assertEquals(12.0, TimeUtils.parseTime("1.5d"))
        assertEquals(4.0, TimeUtils.parseTime("0.5d"))
        assertEquals(16.0, TimeUtils.parseTime("2d"))
    }

    @Test
    fun `parseTime should parse weeks correctly`() {
        assertEquals(40.0, TimeUtils.parseTime("1w"))
        assertEquals(20.0, TimeUtils.parseTime("0.5w"))
        assertEquals(80.0, TimeUtils.parseTime("2w"))
    }

    @Test
    fun `parseTime should handle uppercase and whitespace`() {
        assertEquals(2.0, TimeUtils.parseTime("2H"))
        assertEquals(8.0, TimeUtils.parseTime("1D"))
        assertEquals(40.0, TimeUtils.parseTime("1W"))
        assertEquals(2.0, TimeUtils.parseTime("  2h  "))
        assertEquals(8.0, TimeUtils.parseTime("  1d  "))
    }

    @Test
    fun `parseTime should return null for invalid formats`() {
        assertNull(TimeUtils.parseTime(""))
        assertNull(TimeUtils.parseTime("  "))
        assertNull(TimeUtils.parseTime("2"))
        assertNull(TimeUtils.parseTime("h"))
        assertNull(TimeUtils.parseTime("2x"))
        assertNull(TimeUtils.parseTime("abc"))
        assertNull(TimeUtils.parseTime("2.5.5h"))
    }

    @Test
    fun `formatTime should format hours correctly`() {
        assertEquals("2h", TimeUtils.formatTime(2.0))
        assertEquals("2.5h", TimeUtils.formatTime(2.5))
        assertEquals("0h", TimeUtils.formatTime(0.0))
        assertEquals("10h", TimeUtils.formatTime(10.0))
    }

    @Test
    fun `formatTime should format days when appropriate`() {
        assertEquals("1d", TimeUtils.formatTime(8.0))
        assertEquals("1.5d", TimeUtils.formatTime(12.0))
        assertEquals("2d", TimeUtils.formatTime(16.0))
    }

    @Test
    fun `formatTime should format weeks when appropriate`() {
        assertEquals("1w", TimeUtils.formatTime(40.0))
        assertEquals("2w", TimeUtils.formatTime(80.0))
        assertEquals("0.5w", TimeUtils.formatTime(20.0))
    }

    @Test
    fun `formatTime should prefer weeks over days`() {
        assertEquals("1w", TimeUtils.formatTime(40.0))
    }

    @Test
    fun `formatTime should prefer days over hours for multiples of 8`() {
        assertEquals("3d", TimeUtils.formatTime(24.0))
    }

    @Test
    fun `isValidTimeFormat should validate formats correctly`() {
        assertTrue(TimeUtils.isValidTimeFormat("2h"))
        assertTrue(TimeUtils.isValidTimeFormat("1.5d"))
        assertTrue(TimeUtils.isValidTimeFormat("1w"))
        assertTrue(TimeUtils.isValidTimeFormat("0.5h"))

        assertFalse(TimeUtils.isValidTimeFormat(""))
        assertFalse(TimeUtils.isValidTimeFormat("2"))
        assertFalse(TimeUtils.isValidTimeFormat("abc"))
        assertFalse(TimeUtils.isValidTimeFormat("2x"))
    }

    @Test
    fun `parseTime and formatTime should be consistent for common values`() {
        val testCases = listOf("2h", "1d", "1.5d", "1w", "0.5w")

        testCases.forEach { input ->
            val parsed = TimeUtils.parseTime(input)
            val formatted = TimeUtils.formatTime(parsed!!)
            assertEquals(parsed, TimeUtils.parseTime(formatted),
                "Round-trip failed for input: $input")
        }
    }
}
