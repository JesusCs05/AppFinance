package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                FirebaseApp.initializeApp(context)
            } catch (e: Exception) {
                Log.w(TAG, "Standard Firebase initialization failed, attempting fallback options", e)
            }
        }

        // Programmatic fallback options if standard initialization didn't find google-services.json
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:686838452337:android:42d3fd8f573f54470bcbd0")
                    .setApiKey("AIzaSyBgS1BD5lvVt1GCpHKj-t8eohNUXyqmwmM")
                    .setProjectId("gen-lang-client-0002612712")
                    .setStorageBucket("gen-lang-client-0002612712.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d(TAG, "Firebase initialized via fallback options.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Firebase with explicit fallback options", e)
            }
        }

        if (FirebaseApp.getApps(context).isNotEmpty()) {
            if (BuildConfig.DEBUG) {
                val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
                if (!debugToken.isNullOrEmpty()) {
                    try {
                        val persistenceKey = FirebaseApp.getInstance().persistenceKey
                        val prefs = context.getSharedPreferences(
                            "$DEBUG_STORE_PREFIX$persistenceKey",
                            Context.MODE_PRIVATE
                        )
                        prefs.edit()
                            .putString(DEBUG_SECRET_KEY, debugToken)
                            .commit()
                        Log.d(TAG, "App Check debug secret configured successfully.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error configuring debug token", e)
                    }
                }

                try {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        DebugAppCheckProviderFactory.getInstance()
                    )
                    Log.d(TAG, "DebugAppCheckProviderFactory installed.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed installing DebugAppCheckProviderFactory", e)
                }
            } else {
                val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
                if (resId != 0) {
                    val siteKey = context.getString(resId)
                    if (siteKey.isNotBlank()) {
                        try {
                            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                                RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                            )
                            Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed installing RecaptchaAppCheckProviderFactory", e)
                        }
                    }
                }
            }
        }
    } catch (e: Throwable) {
        Log.e(TAG, "Failed during configureAppCheck", e)
    }
}
