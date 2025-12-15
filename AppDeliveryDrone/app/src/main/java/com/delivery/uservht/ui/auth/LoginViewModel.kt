package com.delivery.uservht.ui.auth

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.EdtState
import com.delivery.core.utils.SingleLiveEvent
import com.delivery.core.utils.StringUtils.validateUserId
import com.delivery.core.utils.StringUtils.validatepassword
import com.delivery.uservht.domain.usecase.LoginUseCase
import com.delivery.vht.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val loginUseCase: LoginUseCase,
        private val rxPreferences: RxPreferences,
    ) : BaseViewModel() {
        private val _uiState = MutableStateFlow(LoginViewState())
        val uiState: StateFlow<LoginViewState> = _uiState.asStateFlow()

        private val _uiEvent = SingleLiveEvent<LoginViewEvent>()
        val uiEvent: SingleLiveEvent<LoginViewEvent> = _uiEvent

        fun handleEvent(event: LoginEvent) {
            when (event) {
                is LoginEvent.PerformLogin -> performLogin(event.username, event.password)
                is LoginEvent.TogglePasswordVisibility -> togglePasswordVisibility()
                is LoginEvent.NavigateToForgotPassword -> navigateToForgotPassword()
                is LoginEvent.NavigateToRegister -> navigateToRegister()
            }
        }

        fun setUserToken(username: String) =
            viewModelScope.launch(Dispatchers.IO) {
                rxPreferences.setUserToken(username)
            }

        private fun performLogin(
            username: String,
            password: String,
        ) {
            isLoading.postValue(true)
            viewModelScope.launch {
                if (!validateInputs(username, password)) {
                    isLoading.postValue(false)
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLoading = true)

                try {
                    // Call UseCase
                    val result = loginUseCase.execute(username, password)

                    result.fold(
                        onSuccess = { token ->
                            _uiEvent.postValue(LoginViewEvent.ShowMessageRes(com.delivery.core.R.string.login_success))
                            _uiEvent.postValue(LoginViewEvent.NavigateToHome)
                        },
                        onFailure = { exception ->
                            when (exception) {
                                is IllegalArgumentException -> {
                                    if (exception.message?.contains("Invalid credentials") == true) {
                                        _uiEvent.postValue(
                                            LoginViewEvent.ShowMessageRes(com.delivery.core.R.string.login_error_invalid_credentials),
                                        )
                                    } else if (exception.message != null) {
                                        _uiEvent.postValue(LoginViewEvent.ShowMessage(exception.message!!))
                                    } else {
                                        _uiEvent.postValue(LoginViewEvent.ShowMessageRes(com.delivery.core.R.string.login_error_validation))
                                    }
                                }
                                else -> {
                                    handleError(exception) { errorResponse ->
                                        if (errorResponse.message != null) {
                                            _uiEvent.postValue(LoginViewEvent.ShowMessage(errorResponse.message!!))
                                        } else {
                                            _uiEvent.postValue(
                                                LoginViewEvent.ShowMessageRes(com.delivery.core.R.string.login_error_generic),
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    )
                } catch (exception: Exception) {
                    handleError(exception) { errorResponse ->
                        if (errorResponse.message != null) {
                            _uiEvent.postValue(LoginViewEvent.ShowMessage(errorResponse.message!!))
                        } else {
                            _uiEvent.postValue(LoginViewEvent.ShowMessageRes(com.delivery.core.R.string.login_error_generic))
                        }
                    }
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    isLoading.postValue(false)
                }
            }
        }

        private fun validateInputs(
            username: String,
            password: String,
        ): Boolean {
            var isValid = true
            var usernameError: Int? = null
            var passwordError: Int? = null

            val usernameValidation = username.validateUserId()
            when (usernameValidation) {
                EdtState.EDT_EMPTY -> {
                    usernameError = R.string.error_username_empty
                    isValid = false
                }
                EdtState.EDT_NOT_HALFWIDTH_OR_DIGIT -> {
                    usernameError = R.string.error_username_invalid_format
                    isValid = false
                }
                EdtState.EDT_LENGTH_INVALID -> {
                    usernameError = R.string.error_username_length_invalid
                    isValid = false
                }
            }

            val passwordValidation = password.validatepassword()
            when (passwordValidation) {
                EdtState.EDT_EMPTY -> {
                    passwordError = R.string.error_password_empty
                    isValid = false
                }
                EdtState.EDT_NOT_HALFWIDTH_OR_DIGIT -> {
                    passwordError = R.string.error_password_invalid_format
                    isValid = false
                }
                EdtState.EDT_LENGTH_INVALID -> {
                    passwordError = R.string.error_password_length_invalid
                    isValid = false
                }
            }

            _uiState.value =
                _uiState.value.copy(
                    usernameError = usernameError,
                    passwordError = passwordError,
                )

            return isValid
        }

        private fun togglePasswordVisibility() {
            _uiState.value =
                _uiState.value.copy(
                    isPasswordVisible = !_uiState.value.isPasswordVisible,
                )
        }

        private fun navigateToForgotPassword() {
            _uiEvent.postValue(LoginViewEvent.NavigateToForgotPassword)
        }

        private fun navigateToRegister() {
            _uiEvent.postValue(LoginViewEvent.NavigateToRegister)
        }
    }

// MVI State
data class LoginViewState(
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val usernameError: Int? = null,
    val passwordError: Int? = null,
)

// MVI Events (User Actions)
sealed class LoginEvent {
    data class PerformLogin(val username: String, val password: String) : LoginEvent()

    data object TogglePasswordVisibility : LoginEvent()

    data object NavigateToForgotPassword : LoginEvent()

    data object NavigateToRegister : LoginEvent()
}

// MVI View Events (Navigation/UI Effects)
sealed class LoginViewEvent {
    data class ShowMessage(val message: String) : LoginViewEvent()

    data class ShowMessageRes(val messageResId: Int) : LoginViewEvent()

    object NavigateToHome : LoginViewEvent()

    object NavigateToForgotPassword : LoginViewEvent()

    object NavigateToRegister : LoginViewEvent()
}
