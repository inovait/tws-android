package si.inova.tws.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import si.inova.tws.example.ui.theme.Example1Theme
import si.inova.tws.manager.TWSFactory
import si.inova.tws.manager.mapData

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Example1Theme {
                val manager = TWSFactory.get(this)

                // collect snippets for your project
                val content = manager.snippets
                    .collectAsStateWithLifecycle(null).value
                    ?.mapData { data ->
                        // sort your tabs with custom key set in snippet properties
                        data.sortedBy {
                            it.props["tabSortKey"] as? String
                        }
                    }

                content?.let {
                    ContentScreen(it)
                }
            }
        }
    }
}
