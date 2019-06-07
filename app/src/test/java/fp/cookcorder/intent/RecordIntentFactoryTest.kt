package fp.cookcorder.intent

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import fp.cookcorder.ReplaceJavaSchedulers
import fp.cookcorder.domain.record.RecordUseCase
import fp.cookcorder.intent.RecordIntentFactory.Companion.recorderIntent
import fp.cookcorder.intentmodel.RecordModelStore
import fp.cookcorder.intentmodel.RecorderState
import fp.cookcorder.intentmodel.RecorderState.*
import fp.cookcorder.model.Task
import fp.cookcorder.view.RecordViewEvent
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.lang.IllegalStateException

class RecordIntentFactoryTest {

    val testScheduler = TestScheduler()

    @get:Rule
    val schedulerRule = ReplaceJavaSchedulers(testScheduler)

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    lateinit var recordModelStore: RecordModelStore

    @Mock
    lateinit var recordUseCase: RecordUseCase

    lateinit var recordIntentFactory: RecordIntentFactory

    @Before
    fun setUp() {
        recordModelStore = RecordModelStore()
        recordIntentFactory = RecordIntentFactory(recordUseCase, recordModelStore)
    }

    @Test
    fun `start recording`() {
        // given
        val testObserver = TestObserver<RecorderState>()

        Mockito.`when`(recordUseCase.startRecordingNewTask())
                .thenReturn(Observable.just(0, 100))
        recordModelStore.modelState().subscribe(testObserver)


        // when
        recordIntentFactory.process(RecordViewEvent.StartRecordingClick)

        testScheduler.triggerActions()

        // then
        // initial
        testObserver.assertValueAt(0, Idle)
        // after just after trying to record
        testObserver.assertValueAt(1, Idle)
        // first tick
        testObserver.assertValueAt(2, Recording(0))
        // second tick
        testObserver.assertValueAt(3, Recording(100))
    }

    @Test
    fun `cancel recording`() {
        // given
        val testObserver = TestObserver<RecorderState>()

        Mockito.`when`(recordUseCase.cancelRecordingNewTask()).thenReturn(Maybe.just(Any()))
        recordModelStore.process(intent {
            Recording(500)
        })
        recordModelStore.modelState().subscribe(testObserver)

        // when
        recordIntentFactory.process(RecordViewEvent.CancelRecordingClick)
        testScheduler.triggerActions()

        // then
        testObserver.assertValueCount(5)
        // initially
        testObserver.assertValueAt(0, Idle)
        testObserver.assertValueAt(1, Recording(500))
        // just after cancellation state should not change
        testObserver.assertValueAt(2, Recording(500))
        //  cancelled
        testObserver.assertValueAt(3, Cancelled)
        // immediately move to idle
        testObserver.assertValueAt(4, Idle)
    }

    @Test
    fun `finish recording`() {
        // given
        val testObserver = TestObserver<RecorderState>()

        Mockito.`when`(recordUseCase.finishRecordingNewTask(any(), any(), any()))
                .thenReturn(Maybe.just(mock()))

        recordModelStore.process(intent {
            Recording(500)
        })

        recordModelStore.modelState().subscribe(testObserver)

        // when
        recordIntentFactory.process(
                RecordViewEvent.FinishRecordingTask(100, "any", 1)
        )
        testScheduler.triggerActions()

        // then
        testObserver.assertValueCount(5)
        // starts in a recording state
        testObserver.assertValueAt(1, Recording(500))
        testObserver.assertValueAt(2, Recording(500))
        testObserver.assertValueAt(3, Success)
        testObserver.assertValueAt(4, Idle)
    }

    @Test
    fun `finish recording with failed state when unsuccessful`() {
        // given
        val testObserver = TestObserver<RecorderState>()

        Mockito.`when`(recordUseCase.finishRecordingNewTask(any(), any(), any()))
                .thenReturn(Maybe.error(IllegalStateException()))

        recordModelStore.process(intent {
            Recording(500)
        })

        recordModelStore.modelState().subscribe(testObserver)

        // when
        recordIntentFactory.process(
                RecordViewEvent.FinishRecordingTask(100, "any", 1)
        )
        testScheduler.triggerActions()

        // then
        testObserver.assertValueCount(5)
        // starts in a recording state
        testObserver.assertValueAt(1, Recording(500))
        testObserver.assertValueAt(2, Recording(500))
        testObserver.assertValueAt(3, Failed)
        testObserver.assertValueAt(4, Idle)
    }
}