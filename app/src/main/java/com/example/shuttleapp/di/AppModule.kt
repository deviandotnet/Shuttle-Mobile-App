package com.example.shuttleapp.di


import android.app.Application
import androidx.room.Room
import com.example.shuttleapp.config.ShuttleDatabase
import com.example.shuttleapp.data.local.DatabaseEncryption
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import com.example.shuttleapp.data.local.dao.RememberedUserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


// Networking (OkHttp, Retrofit, APIs) lives in NetworkModule.
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    //provide the database
    @Singleton
    @Provides
    fun providesShuttleDatabase(app: Application) : ShuttleDatabase {
        // Encrypts the database at rest (driver PII + pass history); converts an older plaintext file in place.
        val passphrase = DatabaseEncryption.prepare(app)
        return Room.databaseBuilder(
            app,
            ShuttleDatabase::class.java,
            DatabaseEncryption.DATABASE_NAME
        )
            .apply { passphrase?.let { openHelperFactory(SupportOpenHelperFactory(it)) } }
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    }

    @Provides
    fun providesRememberedUserDao(database: ShuttleDatabase) : RememberedUserDao {
        return database.RememberedUserDao()
    }
}
