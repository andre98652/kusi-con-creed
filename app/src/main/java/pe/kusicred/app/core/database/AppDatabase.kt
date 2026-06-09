package pe.kusicred.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pe.kusicred.app.core.database.dao.*
import pe.kusicred.app.core.database.entity.*

@Database(
    entities = [
        ChildEntity::class,
        GrowthRecordEntity::class,
        WhoTableEntity::class,
        VaccineCatalogEntity::class,
        VaccineRecordEntity::class,
        IronRecordEntity::class,
        MilestoneCatalogEntity::class,
        MilestoneResponseEntity::class,
        AppPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun childDao(): ChildDao
    abstract fun growthRecordDao(): GrowthRecordDao
    abstract fun whoTableDao(): WhoTableDao
    abstract fun vaccineCatalogDao(): VaccineCatalogDao
    abstract fun vaccineRecordDao(): VaccineRecordDao
    abstract fun ironRecordDao(): IronRecordDao
    abstract fun milestoneCatalogDao(): MilestoneCatalogDao
    abstract fun milestoneResponseDao(): MilestoneResponseDao
    abstract fun appPreferenceDao(): AppPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, seeder: DatabaseSeeder? = null): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kusicred_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seeder?.let {
                                CoroutineScope(Dispatchers.IO).launch {
                                    it.seedDatabase()
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
