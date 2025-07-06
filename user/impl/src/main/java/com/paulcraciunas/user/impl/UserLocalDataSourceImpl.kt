package com.paulcraciunas.user.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserLocalDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserLocalDataSource {
    private val dataStore: DataStore<User> = context.userDataStore

    override fun userUpdates(): Flow<User> = dataStore.data
        .catch { exception ->
            // Handle any exceptions and emit default user
            if (exception is SerializationException) {
                clearUserData()
                emit(User())
            } else {
                throw exception
            }
        }

    override suspend fun getUser(): User = userUpdates().first()

    override suspend fun saveUser(user: User) {
        dataStore.updateData { user }
    }

    override suspend fun updateUser(updater: (User) -> User) {
        dataStore.updateData { currentUser ->
            updater(currentUser)
        }
    }

    override suspend fun clearUserData() {
        dataStore.updateData { User() }
    }
}

private object UserSerializer : Serializer<User> {
    override val defaultValue: User = User()

    override suspend fun readFrom(input: InputStream): User = try {
        Json.decodeFromString(User.serializer(), input.readBytes().decodeToString())
    } catch (serialization: SerializationException) {
        defaultValue
    }

    override suspend fun writeTo(t: User, output: OutputStream) = withContext(Dispatchers.IO) {
        output.write(Json.encodeToString(User.serializer(), t).encodeToByteArray())
    }
}

// DataStore property extension
private val Context.userDataStore: DataStore<User> by dataStore(
    fileName = "user_data.json",
    serializer = UserSerializer
)
