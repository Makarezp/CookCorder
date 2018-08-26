package fp.cookcorder.screen.editdialog

import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.app.ViewModelProviderFactory
import javax.inject.Named

@Module
abstract class EditDialogModule {

    @ContributesAndroidInjector(modules = [EditDialogModuleInternals::class])
    abstract fun provideDialog(): EditDialog

}


@Module
class EditDialogModuleInternals() {


    @Provides
    fun provideVmFactory(vm: EditDialogViewModel) = ViewModelProviderFactory(vm)

    @Provides
    @Named(NAMED_TASK_ID)
    fun provideTaskId(editDialog: EditDialog) = editDialog.arguments!!.getLong(EditDialog.KEY_TASK_ID)

    companion object {
        const val NAMED_TASK_ID = "NAMED_TASK_ID"
    }
}