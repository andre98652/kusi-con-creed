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
    private val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()

    suspend fun isUserRegistered(): Boolean = withContext(Dispatchers.IO) {
        prefDao.getValue("auth_email") != null || firebaseAuth.currentUser != null
    }

    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        val isGuest = prefDao.getValue("auth_is_guest_mode") == "true"
        isGuest || firebaseAuth.currentUser != null
    }

    suspend fun registerUser(name: String, email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = kotlinx.coroutines.tasks.await(firebaseAuth.createUserWithEmailAndPassword(email.trim(), password))
            val user = result.user
            if (user != null) {
                prefDao.upsert(AppPreferenceEntity("auth_name", name))
                prefDao.upsert(AppPreferenceEntity("auth_email", email.trim().lowercase()))
                prefDao.upsert(AppPreferenceEntity("auth_is_guest_mode", "false"))
                prefDao.upsert(AppPreferenceEntity("auth_is_logged_in", "true"))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al registrar cuenta en Firebase."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Error desconocido en Firebase"))
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = kotlinx.coroutines.tasks.await(firebaseAuth.signInWithEmailAndPassword(email.trim(), password))
            if (result.user != null) {
                prefDao.upsert(AppPreferenceEntity("auth_email", email.trim().lowercase()))
                prefDao.upsert(AppPreferenceEntity("auth_is_guest_mode", "false"))
                prefDao.upsert(AppPreferenceEntity("auth_is_logged_in", "true"))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al iniciar sesión en Firebase."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Correo o contraseña incorrectos."))
        }
    }

    suspend fun loginAsGuest(): Result<Unit> = withContext(Dispatchers.IO) {
        prefDao.upsert(AppPreferenceEntity("auth_is_guest_mode", "true"))
        prefDao.upsert(AppPreferenceEntity("auth_is_logged_in", "true"))
        Result.success(Unit)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        firebaseAuth.signOut()
        prefDao.upsert(AppPreferenceEntity("auth_is_logged_in", "false"))
        prefDao.upsert(AppPreferenceEntity("auth_is_guest_mode", "false"))
    }

    suspend fun getRegisteredName(): String? = withContext(Dispatchers.IO) {
        prefDao.getValue("auth_name") ?: firebaseAuth.currentUser?.displayName
    }
}
