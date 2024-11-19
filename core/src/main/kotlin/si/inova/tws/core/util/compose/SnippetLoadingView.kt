package si.inova.tws.core.util.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays a loading view for snippets,
 * with support for both full-screen and card-sized layouts.
 *
 * @param fullScreen A Boolean flag indicating whether the loading view should occupy the full screen (`true`)
 * or a constrained card-sized layout (`false`).
 */
@Composable
internal fun SnippetLoadingView(fullScreen: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .then(if (fullScreen) Modifier.fillMaxHeight() else Modifier.height(200.dp))
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Preview
@Composable
fun SnippetLoadingFullScreenPreview() {
    SnippetLoadingView(fullScreen = true)
}

@Preview
@Composable
fun SnippetLoadingCardPreview() {
    SnippetLoadingView(fullScreen = false)
}
