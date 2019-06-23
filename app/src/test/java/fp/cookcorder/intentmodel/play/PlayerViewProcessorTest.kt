package fp.cookcorder.intentmodel.play

import com.nhaarman.mockito_kotlin.any
import fp.cookcorder.ReplaceJavaSchedulersWithTestScheduler
import fp.cookcorder.intentmodel.intent
import fp.cookcorder.intentmodel.play.PlayerViewEvent.PlayTask
import fp.cookcorder.intentmodel.play.PlayerViewEvent.StopPlayingTask
import fp.cookcorder.intentmodel.play.TaskStatus.NotPlaying
import fp.cookcorder.intentmodel.play.TaskStatus.Playing
import fp.cookcorder.interactors.managetask.TaskInteractor
import fp.cookcorder.interactors.model.Task
import fp.cookcorder.interactors.play.PlayerInteractor
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class PlayerViewProcessorTest {

    val testScheduler = TestScheduler()

    @get:Rule
    val schedulerRule = ReplaceJavaSchedulersWithTestScheduler(testScheduler)

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private lateinit var playerModelStore: PlayerModelStore

    @Mock
    lateinit var taskInteractor: TaskInteractor

    @Mock
    lateinit var playerInteractor: PlayerInteractor

    private lateinit var playerViewProcessor: PlayerViewProcessor

    lateinit var testObserver: TestObserver<PlayerState>

    @Before
    fun setUp() {
        playerModelStore = PlayerModelStore()
        testObserver = TestObserver()
        playerModelStore.modelState().subscribe(testObserver)
    }

    private fun taskFactory(id: Long): Task = Task(
            id, "Title", "FilePath", 100, 400000
    )

    @Test
    fun `load past tasks into modelstore`() {
        // GIVEN
        Mockito.`when`(taskInteractor.getPastTasks()).thenReturn(Flowable.just(listOf(
                taskFactory(1), taskFactory(2)
        )))
        Mockito.`when`(taskInteractor.getCurrentTasks()).thenReturn(Flowable.empty())
        playerViewProcessor = PlayerViewProcessor(playerInteractor, taskInteractor, playerModelStore)

        // WHEN
        testScheduler.triggerActions()

        // THEN
        val taskState = testObserver.values()[1]
        assert(taskState.pastTaskStates.size == 2)
        taskState.pastTaskStates.apply {
            assert(get(0).task.id == 1L)
            assert(get(1).task.id == 2L)
            forEach {
                assert(it.taskStatus == NotPlaying)
                assert(!it.isCurrent)
            }
        }
    }

    @Test
    fun `load current tasks into modelstore`() {
        // GIVEN
        Mockito.`when`(taskInteractor.getCurrentTasks()).thenReturn(Flowable.just(listOf(
                taskFactory(1), taskFactory(2)
        )))
        Mockito.`when`(taskInteractor.getPastTasks()).thenReturn(Flowable.empty())
        playerViewProcessor = PlayerViewProcessor(playerInteractor, taskInteractor, playerModelStore)

        // WHEN
        testScheduler.triggerActions()

        // THEN
        val taskState = testObserver.values()[1]
        assert(taskState.currentTaskStates.size == 2)
        taskState.currentTaskStates.apply {
            assert(get(0).task.id == 1L)
            assert(get(1).task.id == 2L)
            forEach {
                assert(it.taskStatus == NotPlaying)
                assert(it.isCurrent)
            }
        }
    }

    @Test
    fun `start playing task`() {
        // GIVEN
        val task = taskFactory(1)
        Mockito.`when`(taskInteractor.getCurrentTasks()).thenReturn(Flowable.just(listOf(task)
        ))

        Mockito.`when`(playerInteractor.playTask(any(), any())).thenReturn(Observable.fromArray(
                0, 1, 2, 3
        ))
        Mockito.`when`(taskInteractor.getPastTasks()).thenReturn(Flowable.empty())
        playerViewProcessor = PlayerViewProcessor(playerInteractor, taskInteractor, playerModelStore)

        // WHEN
        testScheduler.triggerActions()
        playerViewProcessor.process(PlayTask(task.id))
        testScheduler.triggerActions()

        // THEN
        with(testObserver) {
            assertValueAt(2) { it.findTaskState(1).taskStatus == NotPlaying }
            assertValueAt(3) { it.findTaskState(1).taskStatus == Playing(0) }
            assertValueAt(4) { it.findTaskState(1).taskStatus == Playing(1) }
            assertValueAt(5) { it.findTaskState(1).taskStatus == Playing(2) }
            assertValueAt(6) { it.findTaskState(1).taskStatus == Playing(3) }
            assertValueAt(7) { it.findTaskState(1).taskStatus == NotPlaying }
        }
    }

    @Test
    fun `stop playing task`() {
        // GIVEN
        val task = taskFactory(1)
        Mockito.`when`(taskInteractor.getCurrentTasks()).thenReturn(Flowable.just(listOf(task)))
        Mockito.`when`(taskInteractor.getPastTasks()).thenReturn(Flowable.empty())
        Mockito.`when`(playerInteractor.stopPlayingTask(any())).thenReturn(Single.just(Any()))
        playerViewProcessor = PlayerViewProcessor(playerInteractor, taskInteractor, playerModelStore)
        testScheduler.triggerActions()
        playerModelStore.process(intent {
            setPlayStatusForTask(task.id, 10)
        })

        // WHEN
        playerViewProcessor.process(StopPlayingTask(task.id))
        testScheduler.triggerActions()

        // THEN
        with(testObserver) {
            assertValueAt(1) { it.findTaskState(1).taskStatus == NotPlaying }
            assertValueAt(3) { it.findTaskState(1).taskStatus == Playing(10) }
            assertValueAt(4) { it.findTaskState(1).taskStatus == NotPlaying }

        }
    }

}