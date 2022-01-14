package com.example.tmdbclient.shared

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "cookies")
val SESSION_ID_KEY = stringPreferencesKey("session_id")

//TODO switch to storing parcellable and checking if the session expired