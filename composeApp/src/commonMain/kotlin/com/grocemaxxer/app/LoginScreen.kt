package com.grocemaxxer.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import grocemaxxer.composeapp.generated.resources.Res
import grocemaxxer.composeapp.generated.resources.grocemaxxer_title_no_background
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private enum class LoginMode { SignIn, SignUp }

private enum class LoginStep { Credentials, MfaCode }

/**
 * Entry screen: the start screen's layout -- logo, wordmark, tagline -- with
 * sign-in in place of the list buttons.
 *
 * Only reachable when [client] reports itself configured, so with no backend
 * wired in the app never shows this and goes straight to the lists.
 */
@Composable
internal fun LoginScreen(
    client: AuthClient,
    auth: AuthRepository,
    scope: CoroutineScope,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(LoginMode.SignIn) }
    var step by remember { mutableStateOf(LoginStep.Credentials) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun handle(outcome: AuthOutcome) {
        when (outcome) {
            is AuthOutcome.MfaRequired -> {
                error = null
                code = ""
                step = LoginStep.MfaCode
            }
            is AuthOutcome.Success -> {
                scope.launch {
                    auth.save(outcome.session)
                    onAuthenticated()
                }
            }
            is AuthOutcome.Failure -> error = outcome.message
        }
    }

    fun submitCredentials() {
        if (busy) return
        busy = true
        scope.launch {
            val outcome = if (mode == LoginMode.SignUp) {
                client.signUp(email.trim(), password)
            } else {
                client.signIn(email.trim(), password)
            }
            busy = false
            handle(outcome)
        }
    }

    fun submitCode() {
        if (busy) return
        busy = true
        scope.launch {
            val outcome = client.submitMfaCode(code.trim())
            busy = false
            handle(outcome)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(28.dp))
        BasketLogo(size = 160.dp)
        Spacer(Modifier.height(28.dp))
        Image(
            painter = painterResource(Res.drawable.grocemaxxer_title_no_background),
            contentDescription = "GroceMaxxer",
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = when (step) {
                LoginStep.Credentials ->
                    if (mode == LoginMode.SignUp) {
                        "Create an account to get started."
                    } else {
                        "Log in to pick up your lists."
                    }
                LoginStep.MfaCode ->
                    "Enter the 6-digit code from your authenticator app."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))

        when (step) {
            LoginStep.Credentials -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Email address") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    colors = themedTextFieldColors(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Password") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    colors = themedTextFieldColors(),
                )
            }
            LoginStep.MfaCode -> {
                OutlinedTextField(
                    value = code,
                    onValueChange = { entered ->
                        code = entered.filter { it.isDigit() }.take(6)
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("000000") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done,
                    ),
                    colors = themedTextFieldColors(),
                )
            }
        }

        error?.let { message ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { if (step == LoginStep.Credentials) submitCredentials() else submitCode() },
            enabled = !busy && when (step) {
                LoginStep.Credentials -> email.isNotBlank() && password.isNotBlank()
                LoginStep.MfaCode -> code.length == 6
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(
                text = when {
                    busy -> "WORKING…"
                    step == LoginStep.MfaCode -> "VERIFY"
                    mode == LoginMode.SignUp -> "CREATE ACCOUNT"
                    else -> "LOG IN"
                },
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
        }

        Spacer(Modifier.height(12.dp))
        when (step) {
            LoginStep.Credentials -> TextButton(
                onClick = {
                    mode = if (mode == LoginMode.SignIn) LoginMode.SignUp else LoginMode.SignIn
                    error = null
                },
                enabled = !busy,
            ) {
                Text(
                    if (mode == LoginMode.SignIn) {
                        "New here? Create an account"
                    } else {
                        "Already have an account? Log in"
                    },
                )
            }
            LoginStep.MfaCode -> TextButton(
                onClick = {
                    step = LoginStep.Credentials
                    error = null
                },
                enabled = !busy,
            ) {
                Text("Back")
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}
