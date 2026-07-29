package pe.kusicred.app.core.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pe.kusicred.app.core.database.AppDatabase
import pe.kusicred.app.core.database.DatabaseSeeder
import pe.kusicred.app.core.database.dao.*
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Singleton
import javax.inject.Provider

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabaseSeeder(databaseProvider: Provider<AppDatabase>): DatabaseSeeder {
        return DatabaseSeeder(databaseProvider)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        seeder: DatabaseSeeder
    ): AppDatabase {
        return AppDatabase.getDatabase(context, seeder)
    }

    @Provides
    fun provideChildDao(database: AppDatabase): ChildDao = database.childDao()

    @Provides
    fun provideGrowthRecordDao(database: AppDatabase): GrowthRecordDao = database.growthRecordDao()

    @Provides
    fun provideWhoTableDao(database: AppDatabase): WhoTableDao = database.whoTableDao()

    @Provides
    fun provideVaccineCatalogDao(database: AppDatabase): VaccineCatalogDao = database.vaccineCatalogDao()

    @Provides
    fun provideVaccineRecordDao(database: AppDatabase): VaccineRecordDao = database.vaccineRecordDao()

    @Provides
    fun provideIronRecordDao(database: AppDatabase): IronRecordDao = database.ironRecordDao()

    @Provides
    fun provideMilestoneCatalogDao(database: AppDatabase): MilestoneCatalogDao = database.milestoneCatalogDao()

    @Provides
    fun provideMilestoneResponseDao(database: AppDatabase): MilestoneResponseDao = database.milestoneResponseDao()

    @Provides
    fun provideAppPreferenceDao(database: AppDatabase): AppPreferenceDao = database.appPreferenceDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
