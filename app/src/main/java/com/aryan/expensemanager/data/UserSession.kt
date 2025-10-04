package com.aryan.expensemanager.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSession @Inject constructor() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentCompany = MutableStateFlow<Company?>(null)
    val currentCompany: StateFlow<Company?> = _currentCompany.asStateFlow()

    fun setUser(user: User?, company: Company?) {
        _currentUser.value = user
        _currentCompany.value = company
    }

    fun clear() {
        _currentUser.value = null
        _currentCompany.value = null
    }
}