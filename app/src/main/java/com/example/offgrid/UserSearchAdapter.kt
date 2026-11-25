/**
 * @file app/src/main/java/com/example/offgrid/UserSearchAdapter.kt
 * @brief Adapter listy wyników wyszukiwania użytkowników.
 * @details Łączy modele SearchUser z nazwą i inicjałami w układzie item_search_user.
 */

package com.example.offgrid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.offgrid.databinding.ItemSearchUserBinding

/**
 * @brief Prezentuje publiczne profile przy użyciu RecyclerView i ListAdapter.
 * @details Różnice między listami są obliczane na podstawie UID oraz zawartości SearchUser.
 */
internal class UserSearchAdapter :
    ListAdapter<SearchUser, UserSearchAdapter.UserViewHolder>(DIFFERENCES) {

    /**
     * @brief Tworzy widok pojedynczego wyniku.
     * @param parent Lista, do której należy tworzony wiersz.
     * @param viewType Typ widoku przekazany przez RecyclerView; adapter używa jednego układu.
     * @return Uchwyt zawierający powiązanie item_search_user.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder =
        UserViewHolder(ItemSearchUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))

    /**
     * @brief Wypełnia wiersz profilem z aktualnej listy.
     * @param holder Uchwyt przygotowywanego wiersza.
     * @param position Pozycja profilu w bieżącej liście adaptera.
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * @brief Przechowuje widoki nazwy i inicjałów pojedynczego użytkownika.
     * @param binding Powiązanie układu pojedynczego wyniku wyszukiwania.
     */
    class UserViewHolder(private val binding: ItemSearchUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * @brief Ustawia nazwę użytkownika oraz inicjały zastępujące zdjęcie.
         * @param user Profil przypisywany do wiersza.
         */
        fun bind(user: SearchUser) {
            binding.userName.text = user.displayName
            binding.userInitials.text = UserSearchText.initials(user.displayName)
        }
    }

    /**
     * @brief Udostępnia wspólne reguły porównywania elementów listy.
     */
    private companion object {
        /**
         * @brief Strategia rozpoznawania tożsamości i zmian zawartości profili.
         */
        val DIFFERENCES = object : DiffUtil.ItemCallback<SearchUser>() {
            /**
             * @brief Porównuje tożsamość dwóch pozycji według UID.
             * @param old Profil z poprzedniej listy.
             * @param new Profil z nowej listy.
             * @return Wartość true, gdy oba profile dotyczą tego samego konta.
             */
            override fun areItemsTheSame(old: SearchUser, new: SearchUser) = old.uid == new.uid
            /**
             * @brief Porównuje wszystkie pola dwóch modeli SearchUser.
             * @param old Profil z poprzedniej listy.
             * @param new Profil z nowej listy.
             * @return Wartość true, gdy zawartość profili nie wymaga odświeżenia wiersza.
             */
            override fun areContentsTheSame(old: SearchUser, new: SearchUser) = old == new
        }
    }
}
