package com.example.upcar

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",")
    }
}

@Dao
interface MotoristaDao {
    @Query("SELECT * FROM motoristas")
    suspend fun getTodos(): List<Motorista>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(motorista: Motorista)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarTodos(motoristas: List<Motorista>)

    @Update
    suspend fun atualizar(motorista: Motorista)

    @Delete
    suspend fun remover(motorista: Motorista)
}

@Dao
interface CaronaDao {
    @Query("SELECT * FROM caronas")
    suspend fun getTodas(): List<Carona>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(carona: Carona)

    @Update
    suspend fun atualizar(carona: Carona)

    @Delete
    suspend fun remover(carona: Carona)
}

@Dao
interface MensagemDao {
    @Query("SELECT * FROM mensagens")
    suspend fun getTodas(): List<Mensagem>

    @Query("SELECT * FROM mensagens WHERE caronaId = :caronaId")
    suspend fun getMensagensDaCarona(caronaId: Int): List<Mensagem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(mensagem: Mensagem)

    @Delete
    suspend fun remover(mensagem: Mensagem)

    @Query("DELETE FROM mensagens WHERE caronaId IN (:caronaIds)")
    suspend fun removerPorCaronas(caronaIds: List<Int>)
}

@Dao
interface PassageiroDao {
    @Query("SELECT * FROM passageiros WHERE id = 1 LIMIT 1")
    suspend fun getPassageiro(): Passageiro?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(passageiro: Passageiro)
}

@Dao
interface AvaliacaoDao {
    @Query("SELECT * FROM avaliacoes")
    suspend fun getTodas(): List<AvaliacaoCorrida>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(avaliacao: AvaliacaoCorrida)
}

@Database(
    entities = [
        Motorista::class,
        Carona::class,
        Mensagem::class,
        Passageiro::class,
        AvaliacaoCorrida::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motoristaDao(): MotoristaDao
    abstract fun caronaDao(): CaronaDao
    abstract fun mensagemDao(): MensagemDao
    abstract fun passageiroDao(): PassageiroDao
    abstract fun avaliacaoDao(): AvaliacaoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "upcar_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
