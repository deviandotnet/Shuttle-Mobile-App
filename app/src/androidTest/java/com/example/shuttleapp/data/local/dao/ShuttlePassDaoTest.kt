package com.example.shuttleapp.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.shuttleapp.config.ShuttleDatabase
import com.example.shuttleapp.data.local.RouteEntity
import com.example.shuttleapp.data.local.ShuttleEntity
import com.example.shuttleapp.data.local.ShuttlePassEntity
import com.example.shuttleapp.data.local.ShuttleProviderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShuttlePassDaoTest {

    private lateinit var db: ShuttleDatabase
    private lateinit var dao: ShuttlePassDao

    @Before
    fun setUp(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShuttleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.ShuttlePassDao()

        // shuttle_pass has foreign keys on routeId -> Route and plateNumber -> shuttles.shuttleId,
        // and shuttles has a foreign key on shuttleProviderId.
        db.ShuttleProviderDao().insertShuttleProvider(
            ShuttleProviderEntity(shuttleProviderId = PROVIDER_ID, providerName = "Provider", isActive = true)
        )
        db.RoutesDao().insertRoute(listOf(RouteEntity(routeId = ROUTE_ID, routeName = "Route", code = "R1")))
        db.ShuttleDao().insertShuttle(
            listOf(ShuttleEntity(shuttleId = SHUTTLE_ID, plateNumber = "ABC 123", shuttleProviderId = PROVIDER_ID))
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun markSynced_setsIsSyncedOnlyForGivenIds(): Unit = runBlocking {
        listOf("a", "b", "c").forEach { dao.insertShuttlePass(pass(id = it)) }

        dao.markSynced(listOf("a", "c"))

        assertEquals(true, dao.getShuttlePassById("a").first()?.isSynced)
        assertEquals(false, dao.getShuttlePassById("b").first()?.isSynced)
        assertEquals(true, dao.getShuttlePassById("c").first()?.isSynced)
    }

    @Test
    fun countLateShuttle_ignoresDrafts(): Unit = runBlocking {
        dao.insertShuttlePass(pass(id = "late-final", isLate = true))
        dao.insertShuttlePass(pass(id = "late-draft", isLate = true, isDraft = true))
        dao.insertShuttlePass(pass(id = "on-time", isLate = false))
        dao.insertShuttlePass(pass(id = "other-driver", isLate = true, driver = "driver-2"))

        assertEquals(1, dao.countLateShuttle(DRIVER_ID).first())
    }

    @Test
    fun getAllShuttlePassFilteredByLate_matchesCountLateShuttle(): Unit = runBlocking {
        dao.insertShuttlePass(pass(id = "late-1", isLate = true))
        dao.insertShuttlePass(pass(id = "late-2", isLate = true, isSynced = true))
        dao.insertShuttlePass(pass(id = "late-draft", isLate = true, isDraft = true))
        dao.insertShuttlePass(pass(id = "on-time", isLate = false))

        val late = dao.getAllShuttlePassFilteredByLate(DRIVER_ID).first()

        assertEquals(dao.countLateShuttle(DRIVER_ID).first(), late.size)
        assertEquals(setOf("late-1", "late-2"), late.map { it.shuttlePass.id }.toSet())
    }

    private fun pass(
        id: String,
        driver: String = DRIVER_ID,
        isLate: Boolean = false,
        isDraft: Boolean = false,
        isSynced: Boolean = false,
    ) = ShuttlePassEntity(
        id = id,
        routeId = ROUTE_ID,
        driver = driver,
        plateNumber = SHUTTLE_ID,
        isLateShuttle = isLate,
        isDraft = isDraft,
        isSynced = isSynced,
    )

    private companion object {
        const val PROVIDER_ID = "provider-1"
        const val ROUTE_ID = "route-1"
        const val SHUTTLE_ID = "shuttle-1"
        const val DRIVER_ID = "driver-1"
    }
}
