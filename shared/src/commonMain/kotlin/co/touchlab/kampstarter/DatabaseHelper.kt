package co.touchlab.kampstarter

import co.touchlab.kampstarter.db.*
import co.touchlab.kampstarter.models.Date
import co.touchlab.kampstarter.models.DateAdapter
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper(sqlDriver: SqlDriver) {
    private val dbRef: KampstarterDb = KampstarterDb(sqlDriver, Lunch.Adapter(DateAdapter()))

    fun selectAllItems(): Query<Breed> = dbRef.tableQueries.selectAll()

    suspend fun insertBreeds(breedNames: List<String>) = withContext(Dispatchers.Default) {
        dbRef.transaction {
            breedNames.forEach { name ->
                dbRef.tableQueries.insertBreed(null, name, 0)
            }
        }
    }

    suspend fun selectById(id: Long): Query<Breed> =
        withContext(Dispatchers.Default) { dbRef.tableQueries.selectById(id) }

    suspend fun deleteAll() = withContext(Dispatchers.Default) {
        dbRef.tableQueries.deleteAll()
    }

    suspend fun updateFavorite(breedId: Long, favorite: Boolean) = withContext(Dispatchers.Default) {
        dbRef.tableQueries.updateFavorite(favorite.toLong(), breedId)
    }

    suspend fun insertPlace(name: String, latitude: Double, longitude: Double) = withContext(Dispatchers.Default) {
        dbRef.lunchQueries.insertPlace(null, name, latitude, longitude)
    }

    suspend fun insertPerson(name: String, phone: String) = withContext(Dispatchers.Default) {
        dbRef.lunchQueries.insertPerson(null, name, phone)
    }

    suspend fun insertLunch(name: String, personId: Long, placeId: Long, pinned: Boolean, date: Date) = withContext(Dispatchers.Default) {
        dbRef.lunchQueries.insertLunch(null, name, personId, placeId, pinned, date)
    }

    fun selectAllPlaces(): Query<Place> = dbRef.lunchQueries.selectAllPlaces()

    fun selectAllPeople(): Query<Person> = dbRef.lunchQueries.selectAllPeople()

    fun selectAllLunches(): Query<Lunch> = dbRef.lunchQueries.selectAll()
}

fun Breed.isFavorited(): Boolean = this.favorite != 0L
internal fun Boolean.toLong(): Long = if (this) 1L else 0L