package si.inova.tws.example

import android.app.Application
import si.inova.tws.example.di.ApplicationComponent
import si.inova.tws.example.di.DaggerApplicationComponent

class Example1 : Application() {
    open val applicationComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}