package fp.cookcorder.intentmodel

import fp.cookcorder.ReplaceJavaSchedulersWithTestScheduler
import fp.cookcorder.intent.intent
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ModelStoreTest {
    val testScheduler = TestScheduler()

    @get:Rule
    val schedulerRule = ReplaceJavaSchedulersWithTestScheduler(testScheduler)

    lateinit var store: ModelStore<Boolean>

    lateinit var testObserver: TestObserver<Boolean>

    @Before
    fun setup() {
        store = ModelStore(false)
        testObserver = TestObserver()
    }

    @Test
    fun `processing instant event emits event state and immediately previous state`() {
        // GIVEN
        val eventIntent = intent<Boolean> {
            true
        }
        store.modelState().subscribe(testObserver)

        // WHEN
        store.processInstantEvent(eventIntent)
        testScheduler.triggerActions()

        // THEN
        testObserver.assertValueAt(0, false)
        testObserver.assertValueAt(1, true)
        testObserver.assertValueAt(2, false)
    }
}