package si.inova.tws.example.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import si.inova.tws.manager.TWSFactory
import si.inova.tws.manager.TWSManager

@Module
abstract class AppModule {
    @Binds
    abstract fun bindToContext(application: Application): Context

    @Module
    companion object {
        @Provides
        fun provideTWSManager(context: Context): TWSManager = TWSFactory.get(context)
    }
}