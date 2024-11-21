package si.inova.tws.example.exampleScreen


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import si.inova.tws.manager.TWSConfiguration
import si.inova.tws.manager.TWSFactory
import si.inova.tws.manager.TWSManager
import si.inova.tws.manager.TWSOutcome
import si.inova.tws.manager.mapData
import si.inova.tws.sample.components.LoadingSpinner
import si.inova.tws.sample.components.OnErrorComponent
import si.inova.tws.sample.components.TWSViewComponentWithTabs

@Composable
fun TWSViewCustomTabsExample() {
    val context = LocalContext.current
    val manager = TWSFactory.get(
        context,
        TWSConfiguration.Basic(organizationId = organizationId, projectId = projectId, apiKey = "apiKey")
    )

    TWSViewCustomTabsContent(manager)
}

@Composable
fun TWSViewCustomTabsContent(
    manager: TWSManager
) {
    // collect snippets for your project
    val content = manager.snippets.collectAsStateWithLifecycle(null).value?.mapData { data ->
            // sort your tabs with custom key set in snippet properties
            data.sortedBy {
                it.props["tabSortKey"] as? String
            }
        }

    content?.let {
        when {
            !content.data.isNullOrEmpty() -> {
                val data = content.data ?: return
                TWSViewComponentWithTabs(data.toImmutableList())
            }

            content is TWSOutcome.Error -> {
                OnErrorComponent()
            }

            content is TWSOutcome.Progress -> {
                LoadingSpinner()
            }
        }
    }
}

private const val organizationId = "examples"
private const val projectId = "example1"