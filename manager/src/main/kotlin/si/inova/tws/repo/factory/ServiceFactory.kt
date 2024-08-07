package si.inova.tws.repo.factory

import okhttp3.OkHttpClient
import si.inova.kotlinova.retrofit.callfactory.ErrorHandler

internal interface ServiceFactory {
    fun <S> create(klass: Class<S>, configuration: ServiceCreationScope.() -> Unit = {}): S
    class ServiceCreationScope(private val defaultErrorHandler: ErrorHandler?) {
        var errorHandler: ErrorHandler? = defaultErrorHandler
        var okHttpCustomizer: (OkHttpClient.Builder.() -> Unit)? = null

        var cache: Boolean = true
    }
}

internal inline fun <reified S> ServiceFactory.create(noinline configuration: ServiceFactory.ServiceCreationScope.() -> Unit = {}): S {
    return create(S::class.java, configuration)
}
