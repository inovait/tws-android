package si.inova.tws.example.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.map
import si.inova.tws.manager.TWSManager
import si.inova.tws.manager.mapData
import javax.inject.Inject

class TWSSnippetsViewModel @Inject constructor(
    private val twsManager: TWSManager
) : ViewModel() {
    // Retrieves snippets and sorts them by custom key [sortTabKey] defined in props
    val snippets = twsManager.snippets.map {
        it.mapData { data ->
            data.sortedBy { snippet -> snippet.props["sortTabKey"] as? String }
        }
    }
}