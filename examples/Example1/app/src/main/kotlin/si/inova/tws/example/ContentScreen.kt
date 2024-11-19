package si.inova.tws.example

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.tws.core.TWSView
import si.inova.tws.data.TWSSnippet
import si.inova.tws.manager.TWSOutcome

@Composable
fun ContentScreen(content: TWSOutcome<List<TWSSnippet>>) {
    when {
        !content.data.isNullOrEmpty() -> {
            WebViewComponent(content.data!!.toImmutableList())
        }

        content is TWSOutcome.Error -> {
            OnErrorComponent()
        }

        content is TWSOutcome.Progress -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
fun OnErrorComponent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(256.dp),
            painter = painterResource(R.drawable.ic_error_24),
            contentDescription = "Error"
        )
        Spacer(Modifier.size(8.dp))
        Text(text = "Oooops, loading failed", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium))
    }
}

@Composable
fun WebViewComponent(
    content: ImmutableList<TWSSnippet>
) {
    var currentTab by remember { mutableIntStateOf(0) }
    val onClick: (Int) -> Unit = {
        currentTab = it
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomTabsRow(content, currentTab, onClick) })
    { padding ->
        TWSView(
            snippet = content[currentTab],
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun BottomTabsRow(
    content: ImmutableList<TWSSnippet>,
    currentTab: Int,
    onClick: (Int) -> Unit
) {
    TabRow(currentTab) {
        content.forEachIndexed { index, item ->
            Tab(
                selected = index == currentTab,
                onClick = { onClick(index) },
                text = (item.props["tabName"] as? String)?.let { { Text(text = it, maxLines = 1) } },
                icon = (item.props["tabIcon"] as? String)?.asTabIconDrawable()?.let {
                    {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = "Tab icon"
                        )
                    }
                }
            )
        }
    }
}

private fun String.asTabIconDrawable(): Int {
    return when (this) {
        "home" -> R.drawable.home
        "search" -> R.drawable.search
        "settings" -> R.drawable.settings
        "news" -> R.drawable.news
        "sports_soccer" -> R.drawable.sports_soccer
        "directions_car" -> R.drawable.directions_car
        "public" -> R.drawable.resource_public
        "map" -> R.drawable.map
        "sunny" -> R.drawable.sunny
        "person" -> R.drawable.person
        "list" -> R.drawable.list
        else -> R.drawable.broken_image
    }
}
