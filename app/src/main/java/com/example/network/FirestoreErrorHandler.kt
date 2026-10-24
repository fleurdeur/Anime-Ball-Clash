package com.example.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write")
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String?
): String {
    val currentUser = try {
        FirebaseAuth.getInstance().currentUser
    } catch (_: Exception) {
        null
    }

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

object FirestoreErrorHandler {

    fun logListenerError(
        exception: Exception,
        operationType: OperationType,
        path: String
    ): String = handleFirestoreError(exception, operationType, path)

    fun logAndRethrow(
        exception: Exception,
        operationType: OperationType,
        path: String
    ): Nothing {
        if (exception is CancellationException) throw exception
        handleFirestoreError(exception, operationType, path)
        throw exception
    }
}
