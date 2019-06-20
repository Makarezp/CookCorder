package fp.cookcorder.intentmodel.play

import fp.cookcorder.ReplaceJavaSchedulersWithTestScheduler
import fp.cookcorder.intentmodel.play.TaskStatus.NotPlaying
import fp.cookcorder.interactors.managetask.TaskInteractor
import fp.cookcorder.interactors.model.Task
import fp.cookcorder.interactors.play.PlayerInteractor
import io.reactivex.Flowable
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
            }
        }
    }

    private fun taskFactory(id: Long): Task = Task(
            id, "Title", "FilePath", 100, 400000
    )

}