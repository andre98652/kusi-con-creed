package pe.kusicred.app.core.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pe.kusicred.app.core.database.entity.*

// ============================================================
// CHILD DAO
// ============================================================
@Dao
interface ChildDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity)

    @Update
    suspend fun updateChild(child: ChildEntity)

    @Delete
    suspend fun deleteChild(child: ChildEntity)

    @Query("SELECT * FROM children ORDER BY createdAtMillis DESC")
    fun getAllChildren(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :id")
    suspend fun getChildById(id: String): ChildEntity?

    @Query("SELECT * FROM children WHERE id = :id")
    fun getChildByIdFlow(id: String): Flow<ChildEntity?>

    @Query("SELECT COUNT(*) FROM children")
    suspend fun getChildCount(): Int
}

// ============================================================
// GROWTH RECORD DAO
// ============================================================
@Dao
interface GrowthRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GrowthRecordEntity)

    @Delete
    suspend fun deleteRecord(record: GrowthRecordEntity)

    @Query("SELECT * FROM growth_records WHERE childId = :childId ORDER BY controlDateMillis DESC")
    fun getRecordsByChild(childId: String): Flow<List<GrowthRecordEntity>>

    @Query("SELECT * FROM growth_records WHERE childId = :childId ORDER BY controlDateMillis DESC LIMIT 1")
    suspend fun getLatestRecord(childId: String): GrowthRecordEntity?

    @Query("SELECT * FROM growth_records WHERE childId = :childId ORDER BY controlDateMillis ASC")
    suspend fun getRecordsByChildAsc(childId: String): List<GrowthRecordEntity>
}

// ============================================================
// WHO TABLE DAO
// ============================================================
@Dao
interface WhoTableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<WhoTableEntity>)

    @Query("SELECT * FROM who_table WHERE sex = :sex AND ageMonths = :ageMonths AND measureType = :measureType LIMIT 1")
    suspend fun getRow(sex: String, ageMonths: Int, measureType: String): WhoTableEntity?

    @Query("SELECT COUNT(*) FROM who_table")
    suspend fun getCount(): Int
}

// ============================================================
// VACCINE CATALOG DAO
// ============================================================
@Dao
interface VaccineCatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vaccines: List<VaccineCatalogEntity>)

    @Query("SELECT * FROM vaccine_catalog ORDER BY scheduledAgeDays, scheduledAgeMonths")
    fun getAllVaccines(): Flow<List<VaccineCatalogEntity>>

    @Query("SELECT * FROM vaccine_catalog WHERE vaccineId = :id")
    suspend fun getVaccineById(id: String): VaccineCatalogEntity?

    @Query("SELECT COUNT(*) FROM vaccine_catalog")
    suspend fun getCount(): Int
}

// ============================================================
// VACCINE RECORD DAO
// ============================================================
@Dao
interface VaccineRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: VaccineRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<VaccineRecordEntity>)

    @Update
    suspend fun updateRecord(record: VaccineRecordEntity)

    @Query("SELECT * FROM vaccine_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): VaccineRecordEntity?

    @Query("SELECT * FROM vaccine_records WHERE childId = :childId ORDER BY scheduledDateMillis ASC")
    fun getRecordsByChild(childId: String): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE childId = :childId AND status = 'PENDING' ORDER BY scheduledDateMillis ASC")
    fun getPendingVaccines(childId: String): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE childId = :childId AND scheduledDateMillis BETWEEN :from AND :to")
    suspend fun getVaccinesInRange(childId: String, from: Long, to: Long): List<VaccineRecordEntity>

    @Query("UPDATE vaccine_records SET status = 'OVERDUE' WHERE status = 'PENDING' AND scheduledDateMillis < :nowMillis")
    suspend fun markOverdueVaccines(nowMillis: Long)
}

// ============================================================
// IRON RECORD DAO
// ============================================================
@Dao
interface IronRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: IronRecordEntity)

    @Query("SELECT * FROM iron_records WHERE childId = :childId ORDER BY dateMillis DESC")
    fun getRecordsByChild(childId: String): Flow<List<IronRecordEntity>>

    @Query("SELECT * FROM iron_records WHERE childId = :childId AND dateMillis BETWEEN :from AND :to")
    suspend fun getRecordsInRange(childId: String, from: Long, to: Long): List<IronRecordEntity>

    @Query("SELECT COUNT(*) FROM iron_records WHERE childId = :childId AND taken = 0 AND dateMillis > :since")
    suspend fun getInactiveDaysCount(childId: String, since: Long): Int

    @Query("SELECT * FROM iron_records WHERE childId = :childId AND dateMillis BETWEEN :dayStart AND :dayEnd LIMIT 1")
    suspend fun getRecordForDay(childId: String, dayStart: Long, dayEnd: Long): IronRecordEntity?
}

// ============================================================
// MILESTONE CATALOG DAO
// ============================================================
@Dao
interface MilestoneCatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(milestones: List<MilestoneCatalogEntity>)

    @Query("SELECT * FROM milestone_catalog WHERE ageMonths = :ageMonths ORDER BY area")
    fun getMilestonesByMonth(ageMonths: Int): Flow<List<MilestoneCatalogEntity>>

    @Query("SELECT * FROM milestone_catalog WHERE ageMonths = :ageMonths ORDER BY area")
    suspend fun getMilestonesByMonthOnce(ageMonths: Int): List<MilestoneCatalogEntity>

    @Query("SELECT * FROM milestone_catalog ORDER BY ageMonths, area")
    fun getAllMilestones(): Flow<List<MilestoneCatalogEntity>>

    @Query("SELECT COUNT(*) FROM milestone_catalog")
    suspend fun getCount(): Int
}

// ============================================================
// MILESTONE RESPONSE DAO
// ============================================================
@Dao
interface MilestoneResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: MilestoneResponseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responses: List<MilestoneResponseEntity>)

    @Query("SELECT * FROM milestone_responses WHERE childId = :childId AND milestoneId = :milestoneId ORDER BY evaluationDateMillis DESC")
    fun getResponsesForMilestone(childId: String, milestoneId: String): Flow<List<MilestoneResponseEntity>>

    @Query("SELECT * FROM milestone_responses WHERE childId = :childId ORDER BY evaluationDateMillis DESC")
    fun getAllResponsesByChild(childId: String): Flow<List<MilestoneResponseEntity>>

    @Query("SELECT * FROM milestone_responses WHERE childId = :childId AND milestoneId = :milestoneId ORDER BY evaluationDateMillis DESC LIMIT 2")
    suspend fun getLastTwoResponses(childId: String, milestoneId: String): List<MilestoneResponseEntity>

    @Query("""
        SELECT milestoneId FROM milestone_responses 
        WHERE childId = :childId AND response = 'NOT_YET' 
        GROUP BY milestoneId 
        HAVING COUNT(*) >= 2
    """)
    suspend fun getPersistentlyNotYetMilestones(childId: String): List<String>
}

// ============================================================
// APP PREFERENCE DAO
// ============================================================
@Dao
interface AppPreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pref: AppPreferenceEntity)

    @Query("SELECT value FROM app_preferences WHERE `key` = :key")
    suspend fun getValue(key: String): String?
}
