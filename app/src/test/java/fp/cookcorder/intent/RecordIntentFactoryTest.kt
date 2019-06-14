package fp.cookcorder.intent

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import fp.cookcorder.ReplaceJavaSchedulersWithTestScheduler
import fp.cookcorder.domain.record.RecordUseCase
import fp.cookcorder.intentmodel.*
import fp.cookcorder.intentmodel.RecorderStatus.*
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
    val schedulerRule = ReplaceJavaSchedulersWithTestScheduler(testScheduler)

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private lateinit var recordModelStore: RecordModelStore

    @Mock
    lateinit var recordUseCase: RecordUseCase

    private lateinit var recordIntentFactory: RecordIntentFactory

    lateinit var testObserver: TestObserver<RecorderStatus>

    @Before
    fun setUp() {
        recordModelStore = RecordModelStore()
        recordIntentFactory = RecordIntentFactory(recordUseCase, recordModelStore)
        testObserver = TestObserver()
    }

    @Test
    fun `start recording`() {
        // GIVEN
        Mockito.`when`(recordUseCase.startRecordingNewTask())
                .thenReturn(Observable.just(0, 100))
        recordModelStore.modelState().map { it.recorderStatus }.subscribe(testObserver)

        // WHEN
        recordIntentFactory.process(RecordViewEvent.StartRecordingClick)

        testScheduler.triggerActions()

        // THEN
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
        // GIVEN
        Mockito.`when`(recordUseCase.cancelRecordingNewTask()).thenReturn(Maybe.just(Any()))
        recordModelStore.applyRecordIntent {
            Recording(500)
        }
        recordModelStore.modelState().map { it.recorderStatus }.subscribe(testObserver)

        // WHEN
        recordIntentFactory.process(RecordViewEvent.CancelRecordingClick)
        testScheduler.triggerActions()

        // THEN
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
        // GIVEN
        Mockito.`when`(recordUseCase.finishRecordingNewTask(any(), any(), any()))
                .thenReturn(Maybe.just(mock()))

        recordModelStore.applyRecordIntent { Recording(500) }

        recordModelStore.modelState().map { it.recorderStatus }.subscribe(testObserver)

        // WHEN
        recordIntentFactory.process(
                RecordViewEvent.FinishRecordingClick(100, "any", 1)
        )
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueCount(5)
        // starts in a recording state
        testObserver.assertValueAt(1, Recording(500))
        testObserver.assertValueAt(2, Recording(500))
        testObserver.assertValueAt(3, Success)
        testObserver.assertValueAt(4, Idle)
    }

    @Test
    fun `finish recording with failed state when unsuccessful`() {
        // GIVEN
        Mockito.`when`(recordUseCase.finishRecordingNewTask(any(), any(), any()))
                .thenReturn(Maybe.error(IllegalStateException()))

        recordModelStore.applyRecordIntent { Recording(500) }

        recordModelStore.modelState().map { it.recorderStatus }.subscribe(testObserver)

        // WHEN
        recordIntentFactory.process(
                RecordViewEvent.FinishRecordingClick(100, "any", 1)
        )
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueCount(5)
        // starts in a recording state
        testObserver.assertValueAt(1, Recording(500))
        testObserver.assertValueAt(2, Recording(500))
        testObserver.assertValueAt(3, Failed)
        testObserver.assertValueAt(4, Idle)
    }

    @Test
    fun `keeps idle state when starting record doesn't start`() {
        // GIVEN
        Mockito.`when`(recordUseCase.startRecordingNewTask()).thenReturn(Observable.empty())

        recordModelStore.modelState().map { it.recorderStatus }.subscribe(testObserver)

        // WHEN
        recordIntentFactory.process(RecordViewEvent.StartRecordingClick)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueCount(2)
        // state doesn't change
        testObserver.assertValueAt(0, Idle)
        testObserver.assertValueAt(1, Idle)
    }

    @Test
    fun `when cancelling is unsuccessful state doesn't change`() {
        // GIVEN
        Mockito.`when`(recordUseCase.cancelRecordingNewTask()).thenReturn(Maybe.empty())

        recordModelStore.modelState().map { it.recorderStatus }.subscribe(testObserver)

        recordModelStore.applyRecordIntent {
            Recording(500)
        }

        // WHEN
        recordIntentFactory.process(RecordViewEvent.CancelRecordingClick)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueCount(3)
        testObserver.assertValueAt(1, Recording(500))
        testObserver.assertValueAt(2, Recording(500))
    }
}