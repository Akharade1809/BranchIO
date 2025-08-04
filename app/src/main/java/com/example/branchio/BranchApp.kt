package com.example.branchio

import android.app.Application
import com.example.branchio.di.appModule
import io.branch.referral.Branch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class BranchApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Branch.enableLogging() //

        try{
            Branch.getAutoInstance(this)

            Branch.getInstance().setRequestMetadata("\$analytics_visitor_id", "000001")
        }catch (e: Exception){
            e.printStackTrace()
        }

        startKoin {
            androidLogger()
            androidContext(this@BranchApp)
            modules(appModule)
        }
    }
}