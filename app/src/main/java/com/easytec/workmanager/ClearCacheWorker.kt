package com.easytec.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ClearCacheWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
//        try {
//            applicationContext.cacheDir.listFiles()?.forEach { it.delete() }
//            Log.i("TAG", "doWork:deleted")
//        } catch (e: Exception) {
//            Log.i("TAG", "doWork:failed")
//            return Result.failure()
//        }
        return Result.success()
    }

}
