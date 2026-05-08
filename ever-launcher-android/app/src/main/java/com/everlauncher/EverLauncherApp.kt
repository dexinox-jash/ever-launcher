package com.everlauncher

import android.app.Application
import com.everlauncher.data.db.EverDatabase

class EverLauncherApp : Application() {
    // Eagerly initialize Room so the first frame doesn't pay the init cost
    val database: EverDatabase by lazy { EverDatabase.getInstance(this) }
}
