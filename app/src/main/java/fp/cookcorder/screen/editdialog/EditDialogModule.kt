package fp.cookcorder.screen.editdialog

import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.app.ViewModelProviderFactory

@Module
abstract class EditDialogModule {

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideVmFactory(vm: EditDialogViewModel) = ViewModelProviderFactory(vm)
    }

    @ContributesAndroidInjector
    abstract fun provideDialog(): EditDialog

}