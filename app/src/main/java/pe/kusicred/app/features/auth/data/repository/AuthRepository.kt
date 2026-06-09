package pe.kusicred.app.features.auth.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.kusicred.app.core.database.dao.AppPreferenceDao
import pe.kusicred.app.core.database.entity.AppPreferenceEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val prefDao: AppPreferenceDao
) {
    suspend fun isUserRegistered(): Boolean = withContext(Dispatchers.IO) {
        prefDao.getValue("auth_email") != null
    }

    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        prefDao.getValue("auth_is_logged_in") == "true"
    }

    suspend fun registerUser(name: String, email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        // En una app real, la contraseña debe estar encriptada (bcrypt, etc.) o en la nube.
        // Aquí la guardamos localmente para la simulación.
        prefDao.upsert(AppPreferenceEntity("auth_name", name))
        prefDao.upsert(AppPreferenceEntity("auth_email", email.trim().lowercase()))
        prefDao.upsert(AppPreferenceEntity("auth_password", password))
        prefDao.upsert(AppPreferenceEntity("auth_is_logged_in", "true"))
        Result.success(Unit)
    }

    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        val savedEmail = prefDao.getValue("auth_email")
        val savedPassword = prefDao.getValue("auth_password")

        if (savedEmail == null || savedPassword == null) {
            return@withContext Result.failure(Exception("No hay una cuenta registrada. Por favor regístrate primero."))
        }

        if (email.trim().lowercase() == savedEmail && password == savedPassword) {
            prefDao.upsert(AppPreferenceEntity("auth_is_logged_in", "true"))
            Result.success(Unit)
        } else {
            Result.failure(Exception("Correo o contraseña incorrectos."))
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        prefDao.upsert(AppPreferenceEntity("auth_is_logged_in", "false"))
    }

    suspend fun getRegisteredName(): String? = withContext(Dispatchers.IO) {
        prefDao.getValue("auth_name")
    }
}
