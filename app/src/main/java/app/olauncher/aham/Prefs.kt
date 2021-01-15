package app.olauncher.aham

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val PREFS_FILENAME = "com.olauncher.aham"

    private val FIRST_OPEN = "FIRST_OPEN"
    private val USERNAME = "USERNAME"

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0);

    var firstOpen: Boolean
        get() = prefs.getBoolean(FIRST_OPEN, true)
        set(value) = prefs.edit().putBoolean(FIRST_OPEN, value).apply()

    var username: String
        get() = prefs.getString(USERNAME, "").toString()
        set(value) = prefs.edit().putString(USERNAME, value).apply()
}