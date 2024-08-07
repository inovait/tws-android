package si.inova.tws.repo.factory

import jakarta.inject.Singleton
import retrofit2.Response
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.retrofit.callfactory.ErrorHandler

@Singleton
internal class DefaultErrorHandler : ErrorHandler {
   override fun generateExceptionFromErrorBody(response: Response<*>, parentException: Exception): CauseException? {
      error("Parse errors from your backend here")
   }
}
