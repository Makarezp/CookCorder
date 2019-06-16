package fp.cookcorder.domain.record

import fp.cookcorder.domain.UseCase
import fp.cookcorder.infrastructure.Recorder
import fp.cookcorder.infrastructure.TaskScheduler
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import io.reactivex.Maybe
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface RecordUseCase: UseCase {

    fun startRecordingNewTask(): Observable<Long>

    fun finishRecordingNewTask(
            msToSchedule: Long,
            title: String?,
            repeats: Int): Maybe<Task>

    fun cancelRecordingNewTask(): Maybe<Any>
}

class RecordUseCaseImpl @Inject constructor(
        private val recorder: Recorder,
        private val taskRepo: TaskRepo,
        private val taskScheduler: TaskScheduler) : RecordUseCase {

    override fun startRecordingNewTask(): Observable<Long> {
        return recorder.startRecording("r${Random().nextInt()}")
                .flatMapObservable {
                    Observable.concat(
                            Observable.just(0L),
                            Observable.interval(100, TimeUnit.MILLISECONDS))
                }
    }

    override fun finishRecordingNewTask(msToSchedule: Long,
                                        title: String?,
                                        repeats: Int): Maybe<Task> {
        return recorder.finishRecording()
                .map { record ->
                    val scheduleTime = System.currentTimeMillis() + msToSchedule
                    taskRepo.saveTask(Task(0,
                            title = title,
                            name = record.fileName,
                            duration = record.duration,
                            scheduleTime = scheduleTime,
                            repeats = repeats))
                }
                .doAfterSuccess {
                    taskScheduler.scheduleTask(it)
                }
    }

    override fun cancelRecordingNewTask(): Maybe<Any> = recorder.cancelRecording()
}
