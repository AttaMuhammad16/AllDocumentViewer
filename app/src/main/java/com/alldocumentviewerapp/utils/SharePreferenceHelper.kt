package com.alldocumentviewerapp.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SharedPreferencesHelper @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AttaAllDocumentViewer", Context.MODE_PRIVATE)
    private val editor=prefs.edit()


    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }


    fun putString(key:String,value:String){
        editor.putString(key, value).apply()
    }
    fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue)?:""
    }


    fun putInt(key:String,value:Int){
        editor.putInt(key, value).apply()
    }
    fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)?:0
    }



    fun putLong(key:String,value:Long){
        editor.putLong(key, value).apply()
    }
    fun getLong(key: String, defaultValue: Long): Long {
        return prefs.getLong(key,defaultValue)
    }

    fun clear() {
        editor.clear().apply()
    }
}


