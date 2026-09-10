package com.example.weather.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CliParserTest {

    @Test
    fun `defaults to the four challenge cities and one day`() {
        val options = CliParser.parse(emptyArray())

        assertEquals(listOf("Chisinau", "Madrid", "Kyiv", "Amsterdam"), options.cities)
        assertEquals(1, options.days)
    }

    @Test
    fun `repeated city flags accumulate`() {
        val options = CliParser.parse(arrayOf("-c", "Lisbon", "--city", "Porto"))

        assertEquals(listOf("Lisbon", "Porto"), options.cities)
    }

    @Test
    fun `reads the api key and day count`() {
        val options = CliParser.parse(arrayOf("--api-key", "abc123", "--days", "2"))

        assertEquals("abc123", options.apiKey)
        assertEquals(2, options.days)
    }

    @Test
    fun `rejects a day count the free plan cannot serve`() {
        val e = assertFailsWith<CliException> { CliParser.parse(arrayOf("--days", "5")) }

        assertTrue(e.message!!.contains("between 1 and 2"), e.message!!)
    }

    @Test
    fun `rejects unknown options and missing values`() {
        assertFailsWith<CliException> { CliParser.parse(arrayOf("--nope")) }
        assertFailsWith<CliException> { CliParser.parse(arrayOf("--city")) }
    }

    @Test
    fun `usage lists the default cities`() {
        assertTrue(CliParser.USAGE.contains("Chisinau, Madrid, Kyiv, Amsterdam"), CliParser.USAGE)
    }
}
