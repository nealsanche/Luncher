package co.touchlab.kampstarter.models

import co.touchlab.kampstarter.DatabaseHelper
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

class PersonModel(
    private val viewUpdate: (PersonDataSummary) -> Unit,
    private val errorUpdate: (String) -> Unit
) : BaseModel() {
    private val dbHelper: DatabaseHelper by inject()

    init {
        ensureNeverFrozen()
        scope.launch {
            dbHelper.selectAllPeople().asFlow()
                .map { q ->
                    val itemList = q.executeAsList()
                    PersonDataSummary(itemList.maxBy { it.name.length }, itemList)
                }
                .flowOn(Dispatchers.Default)
                .collect { summary ->
                    viewUpdate(summary)
                }
        }
    }

    fun addPerson(name: String, phone: String): Job =
        scope.launch {
            dbHelper.insertPerson(name, phone)
        }

}

data class PersonDataSummary(val longestItem: Person?, val allItems: List<Person>)
