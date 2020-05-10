package co.touchlab.kampstarter.models

import co.touchlab.kampstarter.DatabaseHelper
import co.touchlab.kampstarter.db.Lunch
import co.touchlab.kampstarter.db.Person
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

class LunchModel(
    private val viewUpdate: (LunchDataSummary) -> Unit,
    private val errorUpdate: (String) -> Unit
) : BaseModel() {
    private val dbHelper: DatabaseHelper by inject()

    init {
        ensureNeverFrozen()
        scope.launch {
            dbHelper.selectAllLunches().asFlow()
                .map { q ->
                    val itemList = q.executeAsList()
                    LunchDataSummary(itemList.maxBy { it.name.length }, itemList)
                }
                .flowOn(Dispatchers.Default)
                .collect { summary ->
                    viewUpdate(summary)
                }
        }
    }

    fun addLunch(person: Person, place: Place, name: String, isPinned: Boolean, date: Date): Job =
        scope.launch {
            dbHelper.insertLunch(name, person.id, place.id, isPinned, date)
        }

}

data class LunchDataSummary(val longestItem: Lunch?, val allItems: List<Lunch>)
