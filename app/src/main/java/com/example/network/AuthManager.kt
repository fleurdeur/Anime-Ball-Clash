package com.example.network

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AuthManager {

    private const val TAG = "Auth"

    fun currentUser(): FirebaseUser? = Firebase.auth.currentUser

    /**
     * Silent auto sign-in check on startup using a single GetGoogleIdOption per request.
     * Falls back cleanly to the interactive "Sign in with Google" button if no authorized
     * credential is available.
     */
    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onAuthSuccess: (FirebaseUser) -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        val existing = Firebase.auth.currentUser
        if (existing != null) {
            onAuthSuccess(existing)
            return
        }

        val activityContext = context.findActivity() ?: context
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "default_web_client_id resource unavailable during auto sign-in", e)
            onUnauthenticated()
            return
        }

        if (clientId.isBlank()) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        // Strictly ONE option per GetCredentialRequest for Android 16 (API 36) responsiveness
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activityContext, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        onAuthSuccess(user)
                    } else {
                        onUnauthenticated()
                    }
                } else {
                    onUnauthenticated()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: NoCredentialException) {
                onUnauthenticated()
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Silent Google Sign-In dismissed: ${e.message}", e)
                onUnauthenticated()
            } catch (e: Exception) {
                Log.w(TAG, "Silent Google Sign-In unavailable: ${e.message}")
                onUnauthenticated()
            }
        }
    }

    /**
     * Interactive "Sign in with Google" flow using a single GetSignInWithGoogleOption per request.
     */
    fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onAuthSuccess: (FirebaseUser) -> Unit,
        onAuthCancelled: () -> Unit,
        onAuthError: (String) -> Unit
    ) {
        val activity = context.findActivity()
        if (activity == null) {
            onAuthError("Unable to open Google Sign-In sheet from non-Activity context.")
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG, "Missing default_web_client_id", e)
            onAuthError("Google Sign-In configuration missing.")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()

        // Strictly ONE option per GetCredentialRequest
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        onAuthSuccess(user)
                    } else {
                        onAuthError("Google Sign-In succeeded without user session.")
                    }
                } else {
                    onAuthError("Unexpected credential type received.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "Sign in failed. Please try again.")
            }
        }
    }

    /**
     * Signs out from Firebase Auth AND clears Credential Manager state.
     */
    fun signOut(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onComplete: () -> Unit = {}
    ) {
        Firebase.auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear credential state: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
