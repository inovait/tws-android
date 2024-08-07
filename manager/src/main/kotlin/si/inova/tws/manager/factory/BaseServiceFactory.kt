package si.inova.tws.manager.factory

import com.squareup.moshi.Moshi
import dispatch.core.DefaultCoroutineScope
import dispatch.core.DispatcherProvider
import jakarta.inject.Singleton
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import si.inova.kotlinova.retrofit.callfactory.ErrorHandlingAdapterFactory
import si.inova.kotlinova.retrofit.callfactory.StaleWhileRevalidateCallAdapterFactory
import si.inova.kotlinova.retrofit.converter.LazyRetrofitConverterFactory
import si.inova.tws.manager.singleton.provideErrorReporter
import si.inova.tws.manager.singleton.twsMoshi
import si.inova.tws.manager.singleton.twsOkHttpClient

@Singleton
internal class BaseServiceFactory : ServiceFactory {
   private val moshi: Moshi by lazy { twsMoshi() }
   private val okHttpClient: OkHttpClient by lazy { twsOkHttpClient() }

   override fun <S> create(
      klass: Class<S>,
      configuration: ServiceFactory.ServiceCreationScope.() -> Unit
   ): S {
      val scope = ServiceFactory.ServiceCreationScope(DefaultErrorHandler())
      configuration(scope)

      val updatedClient = lazy {
         okHttpClient.newBuilder()
            .apply {
               if (scope.cache) {
                  createCache()?.let { cache(it) }
               }
            }
            .apply {
               scope.okHttpCustomizer?.let { it() }
            }
            .build()
      }

      val moshiConverter = lazy {
         MoshiConverterFactory.create(moshi)
      }

      return Retrofit.Builder()
         .callFactory { updatedClient.value.newCall(it) }
         .baseUrl("https://api.thewebsnippet.dev/")
         .addConverterFactory(ScalarsConverterFactory.create())
         .addConverterFactory(LazyRetrofitConverterFactory(moshiConverter))
         .addCallAdapterFactory(
            StaleWhileRevalidateCallAdapterFactory(
               scope.errorHandler,
               provideErrorReporter
            )
         )
         .addCallAdapterFactory(
            ErrorHandlingAdapterFactory(
               DefaultCoroutineScope(object : DispatcherProvider {}),
               scope.errorHandler
            )
         )
         .build()
         .create(klass)
   }

   private fun createCache(): Cache? {
      return null
   }
}
