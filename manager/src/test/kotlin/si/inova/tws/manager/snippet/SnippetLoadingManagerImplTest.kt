package si.inova.tws.manager.snippet

import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import org.junit.Test
import retrofit2.Response
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.tws.manager.TWSConfiguration
import si.inova.tws.manager.utils.FAKE_PROJECT_DTO
import si.inova.tws.manager.utils.FAKE_SHARED_PROJECT
import si.inova.tws.manager.utils.FakeTWSFunctions
import si.inova.tws.manager.utils.MILLISECONDS_DATE
import java.time.Instant
import java.util.Date

internal class SnippetLoadingManagerImplTest {
    private val scope = TestScopeWithDispatcherProvider()

    private val fakeFunctions = FakeTWSFunctions()

    private lateinit var impl: SnippetLoadingManager

    @Test
    fun `Load project`() = scope.runTest {
        impl = SnippetLoadingManagerImpl(
            configuration = TWSConfiguration.Basic("org", "proj", "key"),
            functions = fakeFunctions
        )

        fakeFunctions.returnedProject =
            Response.success(
                FAKE_PROJECT_DTO,
                Headers.Builder().set("date", Date(MILLISECONDS_DATE)).build()
            )

        assert(
            impl.load() == ProjectResponse(
                project = FAKE_PROJECT_DTO,
                responseDate = Instant.ofEpochMilli(MILLISECONDS_DATE),
                null
            )
        )
    }

    @Test
    fun `Load shared snippet`() = scope.runTest {
        impl = SnippetLoadingManagerImpl(
            configuration = TWSConfiguration.Shared("sharedId", "key"),
            functions = fakeFunctions
        )

        fakeFunctions.returnedSharedSnippet = FAKE_SHARED_PROJECT

        fakeFunctions.returnedProject =
            Response.success(
                FAKE_PROJECT_DTO,
                Headers.Builder().set("date", Date(MILLISECONDS_DATE)).build()
            )

        assert(
            impl.load() == ProjectResponse(
                project = FAKE_PROJECT_DTO,
                responseDate = Instant.ofEpochMilli(MILLISECONDS_DATE),
                FAKE_SHARED_PROJECT.snippet.id
            )
        )
    }
}
