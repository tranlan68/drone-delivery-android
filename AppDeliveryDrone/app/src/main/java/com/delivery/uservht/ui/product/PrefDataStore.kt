package com.delivery.uservht.ui.product

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// Khởi tạo DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "product_settings")

object PrefDataStore {

    private val SELECTED_PRODUCTS_KEY = stringPreferencesKey("selected_products")

    suspend fun saveSelectedProducts(context: Context, productsJson: String) {
        context.dataStore.edit {
            it[SELECTED_PRODUCTS_KEY] = productsJson
        }
    }

    suspend fun getSelectedProducts(context: Context): String? {
        val preferences = context.dataStore.data.first()
        return preferences[SELECTED_PRODUCTS_KEY]
    }
}