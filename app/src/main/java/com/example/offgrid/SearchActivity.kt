/**
 * @file app/src/main/java/com/example/offgrid/SearchActivity.kt
 * @brief Ekran wyszukiwania użytkowników i uzupełniania ich profili.
 * @details Obsługuje opóźnione zapytania, stronicowanie, stany błędów oraz brak nazwy użytkownika.
 */

package com.example.offgrid

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.offgrid.databinding.ActivitySearchBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * @brief Wyświetla katalog użytkowników oraz wyniki wyszukiwania po początku nazwy.
 * @details Puste zapytanie pobiera pierwszą stronę katalogu. Wpisany tekst jest normalizowany,
 *          a wyszukiwanie od dwóch punktów kodowych Unicode uruchamia się po 300 ms przerwy.
 *          Liczniki generacji i UID chronią widok przed spóźnionymi odpowiedziami innego zapytania.
 * @see UserProfiles.search
 * @see UserSearchText
 */
class SearchActivity : BaseActivity() {
    /**
     * @brief Powiązanie głównego układu aktywności z widokami.
     */
    private lateinit var binding: ActivitySearchBinding
    /**
     * @brief Powiązanie osadzonego układu screen_search.
     */
    private val screen get() = binding.searchScreen
    /**
     * @brief Bieżąca sesja, której UID jest sprawdzany po zakończeniu operacji.
     */
    private val auth by lazy { FirebaseAuth.getInstance() }
    /**
     * @brief Adapter prezentujący nazwy i inicjały znalezionych użytkowników.
     */
    private val resultsAdapter = UserSearchAdapter()
    /**
     * @brief Kolejka głównego wątku używana do opóźniania wyszukiwania.
     */
    private val handler = Handler(Looper.getMainLooper())
    /**
     * @brief Wskazuje, czy ekran może przyjmować wyniki operacji asynchronicznych.
     */
    private var active = false
    /**
     * @brief Numer wersji zapytania unieważniający starsze odpowiedzi i opóźnione zadania.
     */
    private var queryGeneration = 0
    /**
     * @brief Numer wersji próby synchronizacji własnego profilu.
     */
    private var profileGeneration = 0
    /**
     * @brief Aktualny prefiks wyszukiwania po normalizacji.
     */
    private var query = ""
    /**
     * @brief Zebrane strony wyników z usuniętymi duplikatami UID.
     */
    private var users = emptyList<SearchUser>()
    /**
     * @brief Ostatni pobrany dokument służący do kontynuacji bieżącego zapytania.
     */
    private var cursor: DocumentSnapshot? = null
    /**
     * @brief Blokuje równoczesne pobieranie dwóch stron tej samej wersji zapytania.
     */
    private var loading = false
    /**
     * @brief Zapamiętuje, czy ponowienie dotyczy pierwszej, czy kolejnej strony.
     */
    private var retryNextPage = false
    /**
     * @brief Wybiera uzupełnienie nazwy zamiast zwykłego ponowienia synchronizacji.
     */
    private var missingName = false

    /**
     * @brief Przygotowuje listę wyników, pole zapytania i przyciski ponawiania operacji.
     * @param savedInstanceState Stan poprzedniej instancji aktywności albo null.
     * @details Akcja Search na klawiaturze pomija opóźnienie i zamyka klawiaturę ekranową.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        binding = ActivitySearchBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(screen.bottomNavigation, MainTab.SEARCH)
        screen.searchResults.layoutManager = LinearLayoutManager(this)
        screen.searchResults.adapter = resultsAdapter
        screen.searchQuery.doAfterTextChanged { scheduleSearch() }
        screen.searchQuery.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH) {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(screen.searchQuery.windowToken, 0)
                screen.searchQuery.clearFocus()
                scheduleSearch(immediate = true)
                true
            } else false
        }
        screen.searchRetry.setOnClickListener { loadPage(retryNextPage) }
        screen.searchMore.setOnClickListener { loadPage(nextPage = true) }
        screen.searchProfileRetry.setOnClickListener {
            if (missingName) askForDisplayName() else synchronizeProfile()
        }
    }

    /**
     * @brief Aktywuje ekran, synchronizuje własny profil i uruchamia bieżące zapytanie.
     * @details Operacje są pomijane, gdy ekran jest zamykany lub sesja nie jest dostępna.
     */
    override fun onStart() {
        super.onStart()
        if (isFinishing || !::binding.isInitialized || auth.currentUser == null) return
        active = true
        synchronizeProfile()
        scheduleSearch(immediate = true)
    }

    /**
     * @brief Unieważnia odpowiedzi i usuwa zaplanowane wyszukiwanie po zatrzymaniu ekranu.
     * @details Nie anuluje wysłanych żądań Firestore; ich wyniki zostaną zignorowane przez kontrolę generacji.
     */
    override fun onStop() {
        active = false
        queryGeneration++
        profileGeneration++
        handler.removeCallbacksAndMessages(null)
        loading = false
        super.onStop()
    }

    /**
     * @brief Przygotowuje nową wersję zapytania i czyści wyniki poprzedniej wersji.
     * @param immediate Wartość true uruchamia pobieranie od razu; false stosuje opóźnienie 300 ms.
     * @details Puste pole pozwala przeglądać katalog. Niepusty tekst krótszy niż dwa punkty kodowe
     *          lub pusty po normalizacji pokazuje wskazówkę i nie wysyła zapytania.
     */
    private fun scheduleSearch(immediate: Boolean = false) {
        if (!active) return
        handler.removeCallbacksAndMessages(null)
        val generation = ++queryGeneration
        val typed = screen.searchQuery.text.toString()
        query = UserSearchText.normalize(typed)
        users = emptyList()
        cursor = null
        loading = false
        resultsAdapter.submitList(users)
        screen.searchResultsHeading.setText(
            if (query.isEmpty()) R.string.search_users_heading else R.string.search_results_heading
        )
        screen.searchMore.visibility = View.GONE
        screen.searchRetry.visibility = View.GONE
        if (typed.isNotBlank() && (query.isEmpty() || !UserSearchText.canSearch(query))) {
            screen.searchProgress.visibility = View.GONE
            screen.searchStatus.setText(R.string.search_minimum_characters)
            return
        }
        screen.searchStatus.setText(R.string.search_loading)
        screen.searchProgress.visibility = View.VISIBLE
        if (immediate) loadPage(nextPage = false)
        else handler.postDelayed({
            if (active && generation == queryGeneration) loadPage(nextPage = false)
        }, 300L)
    }

    /**
     * @brief Pobiera pierwszą lub kolejną stronę wyników dla aktualnego prefiksu.
     * @param nextPage Wartość true kontynuuje od zapamiętanego kursora; false zastępuje listę.
     * @details Aktualizuje stan ładowania, liczbę wyników i przycisk kolejnej strony.
     *          Błąd zachowuje już pobrane wyniki, a nieaktualna odpowiedź nie zmienia widoków.
     */
    private fun loadPage(nextPage: Boolean) {
        if (!active || loading) return
        val uid = auth.currentUser?.uid ?: return
        val pageCursor = if (nextPage) cursor ?: return else null
        val generation = queryGeneration
        loading = true
        retryNextPage = nextPage
        screen.searchProgress.visibility = View.VISIBLE
        screen.searchStatus.setText(R.string.search_loading)
        screen.searchRetry.visibility = View.GONE
        screen.searchMore.visibility = View.GONE
        UserProfiles.search(query, pageCursor).addOnCompleteListener { task ->
            if (!isCurrent(generation, uid)) return@addOnCompleteListener
            loading = false
            screen.searchProgress.visibility = View.GONE
            if (task.isSuccessful) {
                val page = task.result
                users = ((if (nextPage) users else emptyList()) + page.users).distinctBy { it.uid }
                cursor = page.cursor
                resultsAdapter.submitList(users)
                screen.searchStatus.text = when {
                    users.isNotEmpty() -> resources.getQuantityString(
                        R.plurals.search_user_count, users.size, users.size
                    )
                    query.isEmpty() -> getString(R.string.search_no_users)
                    else -> getString(R.string.search_no_results)
                }
                screen.searchMore.visibility = if (page.hasMore) View.VISIBLE else View.GONE
            } else {
                val error = task.exception
                Log.w("OffGridSearch", "Could not load users", error)
                screen.searchStatus.setText(searchErrorMessage(error))
                screen.searchRetry.visibility = View.VISIBLE
            }
        }
    }

    /**
     * @brief Sprawdza, czy odpowiedź nadal należy do widocznego ekranu i właściwej sesji.
     * @param generation Numer wersji zapytania zapamiętany przed wysłaniem żądania.
     * @param uid UID konta, które rozpoczęło pobieranie.
     * @return Wartość true, jeżeli aktywność, wersja zapytania i użytkownik nadal są aktualni.
     */
    private fun isCurrent(generation: Int, uid: String): Boolean =
        active && !isFinishing && generation == queryGeneration && auth.currentUser?.uid == uid

    /**
     * @brief Wybiera komunikat dla błędu połączenia albo pozostałych błędów wyszukiwania.
     * @param error Wyjątek zadania; może zawierać błąd Firestore w łańcuchu przyczyn albo być null.
     * @return Identyfikator zasobu tekstowego z komunikatem dla użytkownika.
     */
    private fun searchErrorMessage(error: Exception?): Int {
        val firestoreError = generateSequence<Throwable>(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        return when (firestoreError?.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> R.string.search_connection_error
            else -> R.string.search_failed
        }
    }

    /**
     * @brief Przygotowuje własny profil i prezentuje możliwość ponowienia lub dodania nazwy.
     * @param displayName Nowa nazwa do zapisania albo null, aby użyć dostępnych danych profilu.
     * @details Oddzielny licznik generacji chroni pasek stanu profilu przed starszymi odpowiedziami.
     */
    private fun synchronizeProfile(displayName: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        val generation = ++profileGeneration
        missingName = false
        screen.searchProfileNotice.visibility = View.VISIBLE
        screen.searchProfileStatus.setText(R.string.search_profile_preparing)
        screen.searchProfileRetry.visibility = View.GONE
        UserProfiles.ensureCurrentProfile(displayName).addOnCompleteListener { task ->
            if (!active || isFinishing || generation != profileGeneration
                || auth.currentUser?.uid != uid) return@addOnCompleteListener
            if (task.isSuccessful) {
                screen.searchProfileNotice.visibility = View.GONE
            } else {
                Log.w("OffGridProfiles", "Could not prepare the profile", task.exception)
                missingName = generateSequence<Throwable>(task.exception) { it.cause }
                    .any { it is MissingDisplayNameException }
                screen.searchProfileStatus.setText(
                    if (missingName) R.string.search_profile_name_missing
                    else R.string.search_profile_failed
                )
                screen.searchProfileRetry.setText(
                    if (missingName) R.string.search_add_name else R.string.search_retry
                )
                screen.searchProfileRetry.visibility = View.VISIBLE
            }
        }
    }

    /**
     * @brief Otwiera formularz uzupełnienia brakującej nazwy.
     * @details Sprawdza nazwę i zgodność sesji, zapamiętuje ją lokalnie, a następnie uruchamia
     *          aktualizację Authentication i synchronizację profilu Firestore.
     */
    private fun askForDisplayName() {
        val user = auth.currentUser ?: return
        val input = EditText(this).apply {
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            filters = arrayOf(InputFilter.LengthFilter(UserSearchText.MAX_NAME_LENGTH))
            hint = getString(R.string.search_name_hint)
        }
        val container = FrameLayout(this).apply {
            val padding = (24 * resources.displayMetrics.density).toInt()
            setPadding(padding, 0, padding, 0)
            addView(input, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            ))
        }
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.search_add_name)
            .setView(container)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.search_save, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val name = UserSearchText.displayName(input.text.toString())
                if (!UserSearchText.isValidName(name)) {
                    input.error = getString(R.string.search_name_invalid)
                } else if (auth.currentUser?.uid == user.uid) {
                    UserProfiles.rememberDisplayName(user.uid, name)
                    user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())
                        .addOnFailureListener { Log.w("OffGridProfiles", "Auth name update failed", it) }
                    synchronizeProfile(name)
                    dialog.dismiss()
                } else dialog.dismiss()
            }
        }
        dialog.show()
    }
}
