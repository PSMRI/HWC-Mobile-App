package org.piramalswasthya.cho.ui.login_activity.cho_login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.repositories.UserRepo
import javax.inject.Inject

@HiltViewModel
class ChoLoginViewModel @Inject constructor(private val userRepo: UserRepo): ViewModel() {

}