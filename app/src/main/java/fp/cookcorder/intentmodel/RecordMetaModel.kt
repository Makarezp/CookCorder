package fp.cookcorder.intentmodel

import fp.cookcorder.intent.Intent
import fp.cookcorder.intent.intent
import javax.inject.Inject
import javax.inject.Singleton

class RecordMetaModelStore @Inject constructor(): ModelStore<RecordMetaState>(
        RecordMetaState("", 1)
)

data class RecordMetaState(val title: String, val repeats: Int)

@Singleton
class RecordMetaIntentFactory @Inject constructor(
        private val recordMetaModelStore: RecordMetaModelStore
) {

    fun process(intent: Intent<RecordMetaState>) = recordMetaModelStore.process(intent)

    fun buildChangeNextRecordTitle(title: String) = intent<RecordMetaState> {
        copy(title = title)
    }

    fun buildChangeRepeats(repeats: Int) = intent<RecordMetaState> {
        copy(repeats = repeats)
    }

}