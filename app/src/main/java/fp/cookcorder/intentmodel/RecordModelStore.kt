package fp.cookcorder.intentmodel

import fp.cookcorder.intentmodel.RecorderState.Idle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordModelStore @Inject constructor(): ModelStore<RecorderState>(Idle)