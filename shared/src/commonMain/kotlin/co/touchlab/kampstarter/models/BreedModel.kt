package co.touchlab.kampstarter.models

import co.touchlab.kampstarter.DatabaseHelper
import co.touchlab.kampstarter.currentTimeMillis
import co.touchlab.kampstarter.db.Breed
import co.touchlab.kampstarter.ktor.KtorApi
import co.touchlab.kampstarter.sqldelight.asFlow
import co.touchlab.stately.ensureNeverFrozen
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.inject

class BreedModel(
    private val viewUpdate: (ItemDataSummary) -> Unit,
    private val errorUpdate: (String) -> Unit
) : BaseModel() {
    private val dbHelper: DatabaseHelper by inject()
    private val settings: Settings by inject()
    private val ktorApi: KtorApi by inject()

    companion object {
        internal const val DB_TIMESTAMP_KEY = "DbTimestampKey"
    }

    init {
        ensureNeverFrozen()
        scope.launch {
            dbHelper.selectAllItems().asFlow()
                .map { q ->
                    val itemList = q.executeAsList()
                    ItemDataSummary(itemList.maxBy { it.name.length }, itemList)
                }
                .flowOn(Dispatchers.Default)
                .collect { summary ->
                    viewUpdate(summary)
                }
        }
    }

    fun getBreedsFromNetwork(): Job {
        fun isBreedListStale(currentTimeMS: Long): Boolean {
            val lastDownloadTimeMS = settings.getLong(DB_TIMESTAMP_KEY, 0)
            val oneHourMS = 60 * 60 * 1000
            return (lastDownloadTimeMS + oneHourMS < currentTimeMS)
        }

        val currentTimeMS = currentTimeMillis()
        return if (isBreedListStale(currentTimeMS)) {
            scope.launch {
                try {
                    val breedResult = ktorApi.getJsonFromApi()
                    val breedList = breedResult.message.keys.toList()

                    dbHelper.insertBreeds(breedList)

                    settings.putLong(DB_TIMESTAMP_KEY, currentTimeMS)
                } catch (e: Exception) {
                    errorUpdate("Unable to download breed list")
                }
            }
        } else {
            Job().apply { complete() }
        }
    }

    fun updateBreedFavorite(breed: Breed) = scope.launch {
        dbHelper.updateFavorite(breed.id, breed.favorite != 1L)
    }

}

data class ItemDataSummary(val longestItem: Breed?, val allItems: List<Breed>)
