package fp.cookcorder.domain.record

import fp.cookcorder.domain.UseCase
import fp.cookcorder.infrastructure.Recorder
import fp.cookcorder.infrastructure.TaskScheduler
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import io.reactivex.Maybe
import java.util.*
import javax.inject.Inject

interface RecordUseCase: UseCase {

    fun startRecordingNewTask(): Maybe<Any>

    fun finishRecordingNewTask(msToSchedule: Long, title: String?): Maybe<Task>

    fun cancelRecordingNewTask(): Maybe<Any>

}

class RecordUseCaseImpl @Inject constructor(
        private val recorder: Recorder,
        private val taskRepo: TaskRepo,
        private val taskScheduler: TaskScheduler) : RecordUseCase {

    override fun startRecordingNewTask(): Maybe<Any> {
        return recorder.startRecording("r${Random().nextInt()}")
    }

    override fun finishRecordingNewTask(msToSchedule: Long, title: String?): Maybe<Task> {
        return recorder.finishRecording()
                .map { record ->
                    val scheduleTime = System.currentTimeMillis() + msToSchedule
                    taskRepo.saveTask(Task(0,
                            title = title,
                            name = record.fileName,
                            duration = record.duration,
                            scheduleTime = scheduleTime))
                }
                .doAfterSuccess {
                    taskScheduler.scheduleTask(it)
                }
    }

    override fun cancelRecordingNewTask() = recorder.cancelRecording()

}
