package pe.kusicred.app.features.milestones.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.kusicred.app.core.database.dao.MilestoneCatalogDao
import pe.kusicred.app.core.database.dao.MilestoneResponseDao
import pe.kusicred.app.core.database.entity.MilestoneCatalogEntity
import pe.kusicred.app.core.database.entity.MilestoneResponseEntity
import pe.kusicred.app.core.util.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilestoneRepository @Inject constructor(
    private val catalogDao: MilestoneCatalogDao,
    private val responseDao: MilestoneResponseDao
) {
    fun getMilestonesForMonth(ageMonths: Int): Flow<List<Milestone>> =
        catalogDao.getMilestonesByMonth(ageMonths).map { list -> list.map { it.toDomain() } }

    fun getAllMilestones(): Flow<List<Milestone>> =
        catalogDao.getAllMilestones().map { list -> list.map { it.toDomain() } }

    fun getResponsesByChild(childId: String): Flow<List<MilestoneResponse>> =
        responseDao.getAllResponsesByChild(childId).map { list -> list.map { it.toDomain() } }

    suspend fun saveResponse(
        childId: String,
        milestoneId: String,
        response: MilestoneResponseType,
        date: LocalDate = LocalDate.now()
    ) {
        responseDao.insertResponse(
            MilestoneResponseEntity(
                id = UUID.randomUUID().toString(),
                childId = childId,
                milestoneId = milestoneId,
                evaluationDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                response = response.name
            )
        )
    }

    suspend fun saveAllResponses(
        childId: String,
        responses: Map<String, MilestoneResponseType>,
        date: LocalDate = LocalDate.now()
    ) {
        val entities = responses.map { (milestoneId, response) ->
            MilestoneResponseEntity(
                id = UUID.randomUUID().toString(),
                childId = childId,
                milestoneId = milestoneId,
                evaluationDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                response = response.name
            )
        }
        responseDao.insertAll(entities)
    }

    /**
     * Red Flag Detection: returns milestone IDs that have been "NOT_YET" 2+ consecutive evaluations.
     * Used for premium alerts.
     */
    suspend fun getPersistentlyNotYetMilestones(childId: String): List<String> =
        responseDao.getPersistentlyNotYetMilestones(childId)

    /**
     * Returns milestone IDs that should unlock stimulation guides
     * (marked NOT_YET in the most recent evaluation)
     */
    suspend fun getMilestonesToStimulate(childId: String): List<String> {
        val persistentlyNotYet = responseDao.getPersistentlyNotYetMilestones(childId)
        return persistentlyNotYet
    }

    private fun MilestoneCatalogEntity.toDomain() = Milestone(
        milestoneId = milestoneId, ageMonths = ageMonths,
        area = MilestoneArea.values().find { it.name == area } ?: MilestoneArea.MOTORA,
        question = question, description = description,
        illustration = illustration, stimulationTip = stimulationTip
    )

    private fun MilestoneResponseEntity.toDomain() = MilestoneResponse(
        id = id, childId = childId, milestoneId = milestoneId,
        evaluationDate = LocalDate.ofEpochDay(evaluationDateMillis / (24 * 60 * 60 * 1000L)),
        response = MilestoneResponseType.values().find { it.name == response } ?: MilestoneResponseType.NOT_YET
    )
}
