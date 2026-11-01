package com.example.sweep

import android.app.Application

/**
 * Application class: its lifetime is the whole app process, which makes it
 * the right owner for the [AppContainer].
 */
class SweepApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}