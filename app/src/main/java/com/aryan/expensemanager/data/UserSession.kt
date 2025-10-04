package com.aryan.expensemanager.data

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSession @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentCompany = MutableStateFlow<Company?>(null)
    val currentCompany: StateFlow<Company?> = _currentCompany.asStateFlow()

    fun setUser(user: User?, company: Company?) {
        _currentUser.value = user
        _currentCompany.value = company

        // Persist to SharedPreferences
        prefs.edit().apply {
            if (user != null) {
                putString("user", gson.toJson(user))
            } else {
                remove("user")
            }

            if (company != null) {
                putString("company", gson.toJson(company))
            } else {
                remove("company")
            }
            apply()
        }
    }

    fun restoreSession() {
        val userJson = prefs.getString("user", null)
        val companyJson = prefs.getString("company", null)

        if (userJson != null && companyJson != null) {
            _currentUser.value = gson.fromJson(userJson, User::class.java)
            _currentCompany.value = gson.fromJson(companyJson, Company::class.java)
        }
    }

    fun clear() {
        _currentUser.value = null
        _currentCompany.value = null
        prefs.edit().clear().apply()
    }
}