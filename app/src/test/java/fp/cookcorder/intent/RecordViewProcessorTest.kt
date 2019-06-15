package fp.cookcorder.intent

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import fp.cookcorder.ReplaceJavaSchedulersWithTestScheduler
import fp.cookcorder.domain.record.RecordUseCase
import fp.cookcorder.intentmodel.*
import fp.cookcorder.intentmodel.RecorderState.Event.Empty
import fp.cookcorder.intentmodel.RecorderState.Event.RequestRecordingPermission
import fp.cookcorder.intentmodel.RecorderStatus.*
import fp.cookcorder.view.RecordViewEvent
import fp.cookcorder.view.RecordViewEvent.*
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

class RecordViewProcessorTest {

    val testScheduler = TestScheduler()

    @get:Rule
    val schedulerRule = ReplaceJavaSchedulersWithTestScheduler(testScheduler)

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private lateinit var recordModelStore: RecordModelStore

    @Mock
    lateinit var recordUseCase: RecordUseCase

    private lateinit var recordViewProcessor: RecordViewProcessor

    lateinit var testObserver: TestObserver<RecorderState>

    @Before
    fun setUp() {
        recordModelStore = RecordModelStore()
        recordViewProcessor = RecordViewProcessor(recordUseCase, recordModelStore)
        testObserver = TestObserver()
        recordModelStore.modelState().subscribe(testObserver)
    }

    @Test
    fun `start recording`() {
        // GIVEN
        Mockito.`when`(recordUseCase.startRecordingNewTask())
                .thenReturn(Observable.just(0, 100))

        recordModelStore.process(intent {
            copy(isRecordPermissionGranted = true)
        })

        // WHEN
        recordViewProcessor.process(StartRecordingClick)

        testScheduler.triggerActions()

        // THEN
        // initial
        testObserver.assertValueAt(0) { it.recorderStatus == Idle }
        // after just after trying to record
        testObserver.assertValueAt(2) { it.recorderStatus == Idle }
        // first tick
        testObserver.assertValueAt(3) { it.recorderStatus == Recording(0) }
        // second tick
        testObserver.assertValueAt(4) { it.recorderStatus == Recording(100) }
    }

    @Test
    fun `fires request permission event when start recording without permission granted`() {
        // WHEN
        recordViewProcessor.process(StartRecordingClick)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueAt(2) { it.event == RequestRecordingPermission }
        testObserver.assertValueAt(3) { it.event == Empty }
    }

    @Test
    fun `cancel recording`() {
        // GIVEN
        Mockito.`when`(recordUseCase.cancelRecordingNewTask()).thenReturn(Maybe.just(Any()))
        recordModelStore.applyRecordIntent {
            Recording(500)
        }

        // WHEN
        recordViewProcessor.process(CancelRecordingClick)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueCount(5)
        // initially
        testObserver.assertValueAt(0) { it.recorderStatus == Idle }
        testObserver.assertValueAt(1) { it.recorderStatus == Recording(500) }
        // just after cancellation state should not change
        testObserver.assertValueAt(2) { it.recorderStatus == Recording(500) }
        //  cancelled
        testObserver.assertValueAt(3) { it.recorderStatus == Cancelled }
        // immediately move to idle
        testObserver.assertValueAt(4) { it.recorderStatus == Idle }
    }

    @Test
    fun `finish recording`() {
        // GIVEN
        Mockito.`when`(recordUseCase.finishRecordingNewTask(any(), any(), any()))
                .thenReturn(Maybe.just(mock()))
        recordModelStore.applyRecordIntent { Recording(500) }

        // WHEN
        recordViewProcessor.process(
                FinishRecordingClick(100, "any", 1)
        )
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueCount(5)
        // starts in a recording state
        testObserver.assertValueAt(1) { it.recorderStatus == Recording(500) }
        testObserver.assertValueAt(2) { it.recorderStatus == Recording(500) }
        testObserver.assertValueAt(3) { it.recorderStatus == Success }
        testObserver.assertValueAt(4) { it.recorderStatus == Idle }
    }

    @Test
    fun `finish recording with failed state when unsuccessful`() {
        // GIVEN
        Mockito.`when`(recordUseCase.finishRecordingNewTask(any(), any(), any()))
                .thenReturn(Maybe.error(IllegalStateException()))

        recordModelStore.applyRecordIntent { Recording(500) }

        // WHEN
        recordViewProcessor.process(
                FinishRecordingClick(100, "any", 1)
        )
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueCount(5)
        // starts in a recording state
        testObserver.assertValueAt(1) { it.recorderStatus == Recording(500) }
        testObserver.assertValueAt(2) { it.recorderStatus == Recording(500) }
        testObserver.assertValueAt(3) { it.recorderStatus == Failed }
        testObserver.assertValueAt(4) { it.recorderStatus == Idle }
    }

    @Test
    fun `keeps idle state when starting record doesn't start and permission is granted`() {
        // GIVEN
        Mockito.`when`(recordUseCase.startRecordingNewTask()).thenReturn(Observable.empty())
        recordModelStore.process(intent {
            copy(isRecordPermissionGranted = true)
        })

        // WHEN
        recordViewProcessor.process(StartRecordingClick)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueCount(3)
        // state doesn't change
        testObserver.assertValueAt(0) { it.recorderStatus == Idle }
        // permission granted
        testObserver.assertValueAt(1) { it.recorderStatus == Idle }
        testObserver.assertValueAt(2) { it.recorderStatus == Idle }
    }

    @Test
    fun `when cancelling is unsuccessful state doesn't change`() {
        // GIVEN
        Mockito.`when`(recordUseCase.cancelRecordingNewTask()).thenReturn(Maybe.empty())

        recordModelStore.applyRecordIntent {
            Recording(500)
        }

        // WHEN
        recordViewProcessor.process(CancelRecordingClick)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueCount(3)
        testObserver.assertValueAt(1) { it.recorderStatus == Recording(500) }
        testObserver.assertValueAt(2) { it.recorderStatus == Recording(500) }
    }

    @Test
    fun `change tile view event`() {
        // GIVEN
        val title = "new title"
        val titleTextChangedViewEvent = TitleTextChanged(title)

        // WHEN
        recordViewProcessor.process(titleTextChangedViewEvent)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueAt(1) { it.titleForFinishedRecording == title }
    }

    @Test
    fun `change time to schedule view event`() {
        // GIVEN
        val timeToSchedule = 5
        val timeToScheduleChangedViewEvent = MinsToScheduleChanged(timeToSchedule)

        // WHEN
        recordViewProcessor.process(timeToScheduleChangedViewEvent)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueAt(1) { it.minsToSchedule == timeToSchedule }
    }

    @Test
    fun `record permission granted view event set permission flag on state`() {
        // GIVEN
        val permissionGrantedViewEvent = RecordPermissionGranted(true)

        // WHEN
        recordViewProcessor.process(permissionGrantedViewEvent)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueAt(1) { it.isRecordPermissionGranted }
    }
}