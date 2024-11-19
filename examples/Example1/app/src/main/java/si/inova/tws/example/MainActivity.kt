package si.inova.tws.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import si.inova.tws.example.ui.theme.Example1Theme
import si.inova.tws.example.viewmodel.TWSSnippetsViewModel
import javax.inject.Inject
import javax.inject.Provider

@Keep
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelProvider: Provider<TWSSnippetsViewModel>

    private val viewModel by viewModels<TWSSnippetsViewModel> { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Example1).applicationComponent.inject(this)

        setContent {
            Example1Theme {
                val content = viewModel.snippets.collectAsStateWithLifecycle(null).value

                content?.let {
                    ContentScreen(it)
                }
            }
        }
    }

    private inner class ViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return requireNotNull(viewModelProvider.get()) as T
        }
    }
}
