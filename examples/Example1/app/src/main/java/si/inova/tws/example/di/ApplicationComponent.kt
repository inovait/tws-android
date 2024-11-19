package si.inova.tws.example.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import si.inova.tws.example.MainActivity

@Component(modules = [AppModule::class])
interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            application: Application,
        ): ApplicationComponent
    }

    fun inject(activity: MainActivity)
}