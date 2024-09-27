package com.jw.media.lib.utils.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.jw.media.lib.utils.provider.ContextProvider

/**
 *Created by Joyce.wang on 2024/9/24 15:10
 *@Description TODO
 */
object CommonPreference {
    private val TAG: String = CommonPreference::class.java.simpleName
    private val APP_PREFERENCE_NAME: String = "jw_media"

    private lateinit var mSharedPreferences: SharedPreferences

    init {
        mSharedPreferences = ContextProvider.getContext().getSharedPreferences(APP_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun putStringSet(key: String, values: Set<String?>?) {
        mSharedPreferences.edit().putStringSet(key, values).apply()
    }

    fun putString(key: String, value: String?) {
        mSharedPreferences.edit().putString(key, value).apply()
    }

    fun putInt(key: String, value: Int) {
        mSharedPreferences.edit().putInt(key, value).apply()
    }

    fun putLong(key: String, value: Long) {
        mSharedPreferences.edit().putLong(key, value).apply()
    }

    fun putFloat(key: String, value: Float) {
        mSharedPreferences.edit().putFloat(key, value).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        mSharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getString(key: String): String? {
        return getString(key, "")
    }

    fun getString(key: String, defValue: String?): String? {
        return mSharedPreferences.getString(key, defValue)
    }

    fun getStringSet(key: String, defValues: Set<String?>?): Set<String>? {
        return mSharedPreferences.getStringSet(key, defValues)
    }

    fun getInt(key: String, defValue: Int): Int {
        return mSharedPreferences.getInt(key, defValue)
    }

    fun getLong(key: String, defValue: Long): Long {
        return mSharedPreferences.getLong(key, defValue)
    }

    fun getFloat(key: String, defValue: Float): Float {
        return mSharedPreferences.getFloat(key, defValue)
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return mSharedPreferences.getBoolean(key, defValue)
    }

    @SuppressLint("CommitPrefEdits")
    fun remove(key: String) {
        mSharedPreferences.edit().remove(key)
    }
}