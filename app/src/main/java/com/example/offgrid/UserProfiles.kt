/**
 * @file app/src/main/java/com/example/offgrid/UserProfiles.kt
 * @brief Modele i operacje katalogu użytkowników przechowywanego w Firestore.
 * @details Utrzymuje publiczne dokumenty users/{uid} oraz pobiera kolejne strony wyników wyszukiwania.
 */

package com.example.offgrid

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/**
 * @brief Minimalny publiczny profil wyświetlany na liście wyników.
 * @param uid Identyfikator dokumentu Firestore zgodny z UID konta Authentication.
 * @param displayName Nazwa wyświetlana użytkownikom aplikacji.
 */
internal data class SearchUser(val uid: String, val displayName: String)

/**
 * @brief Wynik pobrania pojedynczej strony katalogu użytkowników.
 * @param users Profile z pominięciem własnego konta oraz dokumentów bez nazwy.
 * @param cursor Ostatni pobrany dokument, także gdy nie trafił na listę users; null dla pustej strony.
 * @param hasMore Informacja, czy pobrano pełną partię i może istnieć następna strona.
 * @note Pełna partia nie gwarantuje istnienia dalszych dokumentów, a odfiltrowanie własnego konta
 *       może zmniejszyć liczbę widocznych profili poniżej limitu pobrania.
 */
internal data class UserSearchPage(
    val users: List<SearchUser>,
    val cursor: DocumentSnapshot?,
    val hasMore: Boolean
)

/**
 * @brief Sygnalizuje brak poprawnej nazwy potrzebnej do utworzenia wyszukiwalnego profilu.
 * @details Błąd pochodzi z transakcji i jest przekazywany przez wynik zadania synchronizacji.
 */
internal class MissingDisplayNameException : IllegalStateException("A display name is required")

/**
 * @brief Łączy sesję Authentication z publicznym katalogiem profili w Firestore.
 * @details Dokumenty zawierają wyłącznie displayName, displayNameSearch i opcjonalne photoUrl.
 *          Nazwy oczekujące na zapis są przechowywane lokalnie, aby umożliwić ponowienie operacji.
 * @see initialize
 * @see ensureCurrentProfile
 */
internal object UserProfiles {
    /**
     * @brief Maksymalna liczba dokumentów pobieranych w jednym zapytaniu katalogu.
     */
    private const val PAGE_SIZE = 20L
    /**
     * @brief Źródło bieżącej sesji i UID użytkownika.
     */
    private val auth by lazy { FirebaseAuth.getInstance() }
    /**
     * @brief Domyślna baza Firestore projektu skonfigurowanego w aplikacji.
     */
    private val database by lazy { FirebaseFirestore.getInstance() }
    /**
     * @brief Preferencje z publicznymi nazwami oczekującymi na zapis, indeksowane UID konta.
     */
    private lateinit var pendingNames: SharedPreferences

    /**
     * @brief Przygotowuje preferencje i obserwuje zmiany sesji użytkownika.
     * @param context Kontekst, z którego pobierany jest kontekst aplikacji i plik preferencji.
     * @details Dla dostępnej sesji podejmuje próbę utworzenia lub uzupełnienia profilu.
     * @note Wywoływane raz przy starcie aplikacji przez OffGridApplication.onCreate.
     */
    fun initialize(context: Context) {
        pendingNames = context.applicationContext.getSharedPreferences(
            "offgrid_pending_profile_names", Context.MODE_PRIVATE
        )
        auth.addAuthStateListener { firebase ->
            if (firebase.currentUser != null) {
                ensureCurrentProfile().addOnFailureListener { error ->
                    Log.w("OffGridProfiles", "Profile synchronization will be retried", error)
                }
            }
        }
    }

    /**
     * @brief Zapamiętuje nazwę do ponownego zapisu w Firestore.
     * @param uid UID konta, którego dotyczy nazwa.
     * @param name Nazwa poddawana oczyszczeniu białych znaków przed zapisem lokalnym.
     * @pre initialize musi zostać wywołane przed dostępem do preferencji.
     */
    fun rememberDisplayName(uid: String, name: String) {
        pendingNames.edit().putString(uid, UserSearchText.displayName(name)).apply()
    }

    /**
     * @brief Tworzy lub ujednolica publiczny dokument bieżącego użytkownika.
     * @param displayNameHint Nowa nazwa zapamiętywana do synchronizacji albo null.
     * @return Zadanie zawierające zapisaną nazwę lub błąd sesji, transakcji albo braku poprawnej nazwy.
     * @pre initialize musi przygotować lokalne preferencje.
     * @details W transakcji wybiera pierwszą poprawną nazwę: oczekującą lokalnie, zapisaną w dokumencie
     *          lub dostępną w Authentication. Odtwarza dokument w schemacie trzech publicznych pól
     *          i zapisuje go tylko wtedy, gdy dane się różnią. Po sukcesie usuwa zgodną oczekującą nazwę.
     * @note Błąd transakcji zachowuje oczekującą nazwę do późniejszego ponowienia.
     */
    fun ensureCurrentProfile(displayNameHint: String? = null): Task<String> {
        val user = auth.currentUser
            ?: return Tasks.forException(IllegalStateException("Sign-in required"))
        val uid = user.uid
        if (displayNameHint != null) rememberDisplayName(uid, displayNameHint)
        val reference = database.collection("users").document(uid)
        val authName = user.displayName.orEmpty()
        val authPhoto = user.photoUrl?.toString()

        val task = database.runTransaction { transaction ->
            val snapshot = transaction.get(reference)
            val rememberedName = pendingNames.getString(uid, null)
            val oldName = snapshot.get("displayName") as? String
            val candidates = listOfNotNull(rememberedName, oldName, authName)
                .map(UserSearchText::displayName)
            val name = candidates.firstOrNull(UserSearchText::isValidName)
                ?: throw MissingDisplayNameException()
            val searchName = UserSearchText.normalize(name)
            val photo = ((snapshot.get("photoUrl") as? String) ?: authPhoto)
                ?.takeIf { it.length <= 2048 }
            val data = mapOf(
                "displayName" to name,
                "displayNameSearch" to searchName,
                "photoUrl" to photo
            )

            if (snapshot.data != data) transaction.set(reference, data)
            name
        }
        task.addOnSuccessListener { savedName ->
            // Do not discard a newer name entered while the transaction was running.
            if (pendingNames.getString(uid, null) == savedName) {
                pendingNames.edit().remove(uid).apply()
            }
        }
        return task
    }

    /**
     * @brief Pobiera stronę katalogu uporządkowaną po znormalizowanej nazwie.
     * @param prefix Prefiks przygotowany przez UserSearchText.normalize; pusty tekst przegląda katalog.
     * @param cursor Ostatni dokument poprzedniej strony tego samego zapytania albo null dla pierwszej strony.
     * @return Zadanie z profilami, kursorem i wskazówką o możliwej kolejnej stronie albo błędem pobierania.
     * @details Stosuje dolną granicę włącznie oraz wyłączną górną granicę prefiksu Unicode.
     *          Pobiera do 20 dokumentów z serwera i odfiltrowuje konto aktualnie zalogowanego użytkownika.
     * @note Pobieranie korzysta z Source.SERVER, dlatego brak połączenia może zakończyć zadanie błędem.
     */
    fun search(prefix: String, cursor: DocumentSnapshot? = null): Task<UserSearchPage> {
        val uid = auth.currentUser?.uid
            ?: return Tasks.forException(IllegalStateException("Sign-in required"))
        var query = database.collection("users").orderBy("displayNameSearch")
        if (prefix.isNotEmpty()) {
            query = query.startAt(prefix)
            UserSearchText.upperBound(prefix)?.let { query = query.endBefore(it) }
        }
        if (cursor != null) query = query.startAfter(cursor)

        return query.limit(PAGE_SIZE).get(Source.SERVER).continueWith { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: IllegalStateException("User search failed")
            }
            val documents = task.result.documents
            val users = documents.mapNotNull { document ->
                val name = document.get("displayName") as? String
                if (document.id == uid || name.isNullOrBlank()) null
                else SearchUser(document.id, name)
            }
            UserSearchPage(
                users = users,
                cursor = documents.lastOrNull(),
                hasMore = documents.size.toLong() == PAGE_SIZE
            )
        }
    }
}
