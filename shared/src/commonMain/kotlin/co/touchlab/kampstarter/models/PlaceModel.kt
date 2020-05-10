package co.touchlab.kampstarter.models

import co.touchlab.kampstarter.DatabaseHelper
import co.touchlab.kampstarter.db.Place
import co.touchlab.kampstarter.sqldelight.asFlow
import co.touchlab.stately.ensureNeverFrozen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.inject

class PlaceModel(
    private val viewUpdate: (PlaceDataSummary) -> Unit,
    private val errorUpdate: (String) -> Unit
) : BaseModel() {
    private val dbHelper: DatabaseHelper by inject()

    init {
        ensureNeverFrozen()
        scope.launch {
            dbHelper.selectAllPlaces().asFlow()
                .map { q ->
                    val itemList = q.executeAsList()
                    PlaceDataSummary(itemList.maxBy { it.name.length }, itemList)
                }
                .flowOn(Dispatchers.Default)
                .collect { summary ->
                    viewUpdate(summary)
                }
        }
    }

    fun addPlace(name: String, latitude: Double, longitude: Double): Job =
        scope.launch {
            dbHelper.insertPlace(name, latitude, longitude)
        }

}

data class PlaceDataSummary(val longestItem: Place?, val allItems: List<Place>)
