/**
 * @file app/src/test/java/com/example/offgrid/UserSearchTextTest.kt
 * @brief Testy normalizacji, walidacji i granic zakresu wyszukiwania.
 * @details Sprawdza polskie znaki, niezależność od języka urządzenia oraz skrajne przypadki Unicode.
 */

package com.example.offgrid

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @brief Weryfikuje kontrakt operacji UserSearchText bez uruchamiania Androida.
 */
class UserSearchTextTest {
    /**
     * @brief Sprawdza polskie litery, wielkość znaków i rozłożone znaki diakrytyczne.
     */
    @Test
    fun matchesPolishNamesWithoutAccentsAndWithDifferentCase() {
        assertEquals("lukasz zolc", UserSearchText.normalize("  ŁUKASZ  Żółć  "))
        assertEquals("anna", UserSearchText.normalize("A\u0301nna"))
    }

    /**
     * @brief Sprawdza niezależność normalizacji od tureckiej lokalizacji i przywraca ustawienia testu.
     */
    @Test
    fun caseFoldingDoesNotDependOnThePhoneLocale() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("iga", UserSearchText.normalize("IGA"))
        } finally {
            Locale.setDefault(previous)
        }
    }

    /**
     * @brief Sprawdza graniczną długość nazwy, brak treści po normalizacji i łączenie białych znaków.
     */
    @Test
    fun namesHaveUsableLengthAndSearchContent() {
        assertTrue(UserSearchText.isValidName("a".repeat(100)))
        assertFalse(UserSearchText.isValidName("a".repeat(101)))
        assertFalse(UserSearchText.isValidName("\u0301\u0308"))
        assertEquals("Jan Kowalski", UserSearchText.displayName(" Jan\t\nKowalski "))
    }

    /**
     * @brief Sprawdza próg wyszukiwania bez liczenia pary surogatów emoji jako dwóch znaków.
     */
    @Test
    fun minimumLengthCountsWholeUnicodeCharacters() {
        assertTrue(UserSearchText.canSearch(""))
        assertFalse(UserSearchText.canSearch("j"))
        assertFalse(UserSearchText.canSearch("😀"))
        assertTrue(UserSearchText.canSearch("ja"))
        assertTrue(UserSearchText.canSearch("😀a"))
    }

    /**
     * @brief Porównuje zakres prefiksowy UTF-8 z oczekiwanym dopasowaniem nazw, także z emoji.
     */
    @Test
    fun prefixRangeIncludesEmojiAndExcludesOtherNames() {
        val names = listOf("ja", "jan", "jan😀", "jan\uFFFF", "jane", "jao", "janek", "jo")
        val prefix = "jan"
        val upper = UserSearchText.upperBound(prefix)!!
        val actual = names.filter {
            utf8Compare(it, prefix) >= 0 && utf8Compare(it, upper) < 0
        }
        assertEquals(names.filter { it.startsWith(prefix) }, actual)
    }

    /**
     * @brief Sprawdza przeniesienie końcowego punktu kodowego oraz pominięcie zakresu surogatów.
     */
    @Test
    fun upperBoundHandlesUnicodeEdges() {
        val maximum = String(Character.toChars(Character.MAX_CODE_POINT))
        assertEquals("jao", UserSearchText.upperBound("jan"))
        assertEquals("\uE000", UserSearchText.upperBound("\uD7FF"))
        assertEquals("b", UserSearchText.upperBound("a$maximum"))
        assertNull(UserSearchText.upperBound(maximum))
    }

    /**
     * @brief Porównuje teksty leksykograficznie według nieujemnych wartości bajtów UTF-8.
     * @param left Tekst po lewej stronie porównania.
     * @param right Tekst po prawej stronie porównania.
     * @return Wartość ujemna, zero lub wartość dodatnia zależnie od kolejności tekstów.
     * @details Umożliwia sprawdzenie granic zakresu bez porównywania kolejności jednostek UTF-16.
     */
    private fun utf8Compare(left: String, right: String): Int {
        val a = left.toByteArray(Charsets.UTF_8)
        val b = right.toByteArray(Charsets.UTF_8)
        for (index in 0 until minOf(a.size, b.size)) {
            val difference = (a[index].toInt() and 255) - (b[index].toInt() and 255)
            if (difference != 0) return difference
        }
        return a.size - b.size
    }
}
