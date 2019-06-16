package fp.cookcorder.interactors.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Task(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val title: String?,
        val name: String,
        var duration: Int,
        var scheduleTime: Long,
        var repeats: Int = 1
)