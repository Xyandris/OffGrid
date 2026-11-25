/**
 * @file app/src/main/java/com/example/offgrid/UserSearchText.kt
 * @brief Przygotowanie nazw użytkowników oraz granic zapytań prefiksowych.
 * @details Wspólne reguły walidacji i normalizacji dla rejestracji, profili Firestore oraz wyszukiwarki.
 */

package com.example.offgrid

import java.text.Normalizer
import java.util.Locale

/**
 * @brief Udostępnia operacje tekstowe używane przez katalog użytkowników.
 * @details Normalizacja nazw i zapytań musi być identyczna, aby dopasowanie prefiksu było poprawne.
 */
internal object UserSearchText {
    /**
     * @brief Maksymalna długość nazwy mierzona przez String.length, czyli liczbę jednostek UTF-16.
     */
    const val MAX_NAME_LENGTH = 100
    /**
     * @brief Minimalna liczba punktów kodowych Unicode dla niepustego zapytania.
     */
    const val MIN_QUERY_LENGTH = 2

    /**
     * @brief Wyrażenie łączące ciągi białych znaków obsługiwanych przez wzorzec regex.
     */
    private val spaces = Regex("\\s+")
    /**
     * @brief Wyrażenie usuwające znaki łączące po dekompozycji Unicode NFD.
     */
    private val marks = Regex("\\p{M}+")

    /**
     * @brief Oczyszcza nazwę z brzegowych białych znaków i nadmiarowych odstępów.
     * @param value Nazwa przed oczyszczeniem.
     * @return Nazwa z ciągami dopasowanych białych znaków zastąpionymi pojedynczą spacją.
     */
    fun displayName(value: String): String = value.trim().replace(spaces, " ")

    /**
     * @brief Przygotowuje nazwę lub zapytanie do porównywania prefiksowego.
     * @param value Tekst nazwy albo wpisane zapytanie.
     * @return Oczyszczony tekst małymi literami, po mapowaniu ł na l i usunięciu znaków łączących.
     * @details Używa Locale.ROOT oraz dekompozycji NFD, aby wynik nie zależał od języka telefonu.
     */
    fun normalize(value: String): String = Normalizer.normalize(
        displayName(value).lowercase(Locale.ROOT).replace('ł', 'l'),
        Normalizer.Form.NFD
    ).replace(marks, "")

    /**
     * @brief Sprawdza długość nazwy oraz obecność treści po normalizacji.
     * @param value Nazwa przeznaczona do zapisania; wywołujący może wcześniej użyć displayName.
     * @return Wartość true dla niepustej nazwy mieszczącej się w limicie i niepustej po normalizacji.
     */
    fun isValidName(value: String): Boolean =
        value.isNotBlank() && value.length <= MAX_NAME_LENGTH && normalize(value).isNotBlank()

    /**
     * @brief Sprawdza, czy prefiks ma długość dopuszczoną przez interfejs wyszukiwarki.
     * @param value Znormalizowany prefiks zapytania.
     * @return Wartość true dla pustego prefiksu albo co najmniej dwóch punktów kodowych Unicode.
     * @note Pusty prefiks oznacza przeglądanie katalogu, a nie wyszukiwanie krótkiej nazwy.
     */
    fun canSearch(value: String): Boolean =
        value.isEmpty() || value.codePointCount(0, value.length) >= MIN_QUERY_LENGTH

    // The next Unicode prefix gives an exclusive upper bound, including emoji names.
    /**
     * @brief Wyznacza wyłączną górną granicę zakresu wszystkich nazw o danym prefiksie.
     * @param prefix Prefiks, dla którego tworzony jest zakres uporządkowany według Unicode.
     * @return Najbliższy prefiks zamykający zakres albo null dla pustego tekstu lub braku następnej granicy.
     * @details Zwiększa ostatni możliwy punkt kodowy, przenosi przepełnienie i pomija zakres surogatów.
     *          Dzięki temu zakres obejmuje także znaki spoza podstawowej płaszczyzny, w tym emoji.
     */
    fun upperBound(prefix: String): String? {
        val points = prefix.codePoints().toArray()
        for (index in points.lastIndex downTo 0) {
            if (points[index] < Character.MAX_CODE_POINT) {
                var next = points[index] + 1
                if (next in 0xD800..0xDFFF) next = 0xE000
                return String(points, 0, index) + String(Character.toChars(next))
            }
        }
        return null
    }

    /**
     * @brief Wyznacza inicjały używane w zastępczym awatarze.
     * @param name Nazwa użytkownika przed oczyszczeniem.
     * @return Wielkie litery utworzone z pierwszych punktów kodowych najwyżej dwóch członów nazwy.
     */
    fun initials(name: String): String = displayName(name).split(' ')
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { String(Character.toChars(it.codePointAt(0))) }
        .uppercase(Locale.ROOT)
}
