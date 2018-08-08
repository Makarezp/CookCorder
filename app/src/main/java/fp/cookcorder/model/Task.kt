package fp.cookcorder.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity
data class Task(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val title: String?,
        val name: String,
        var duration: Int,
        var scheduleTime: Long
)