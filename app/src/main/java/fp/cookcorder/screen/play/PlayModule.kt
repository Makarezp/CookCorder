package fp.cookcorder.screen.play

import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.intentmodel.play.PlayerModelStore
import fp.cookcorder.intentmodel.play.PlayerViewProcessor
import fp.cookcorder.screen.MainActivity
import javax.inject.Named

@Module
abstract class PlayModule {

    @ContributesAndroidInjector(modules = [PlayFragmentModule::class])
    abstract fun bindPlayFragment(): PlayFragment

}

@Module
class PlayFragmentModule {


    @Module
    companion object {
        const val NAMED_IS_CURRENT = "NAMED_IS_CURRENT"

        @JvmStatic
        @Provides
        fun provideVmFactory(vm: PlayViewModel) = ViewModelProviderFactory(vm)
    }

    @Named(NAMED_IS_CURRENT)
    @Provides
    fun provideIsCurrent(fragment: PlayFragment): Boolean {
        return fragment.arguments!!.getBoolean(PlayFragment.KEY_IS_CURRENT)
    }

    @Provides
    fun provideTaskAdapter(@Named(NAMED_IS_CURRENT) isCurrent: Boolean,
                           playerModelStore: PlayerModelStore,
                           playerViewProcessor: PlayerViewProcessor): TaskAdapter {
        return TaskAdapter(isCurrent, playerModelStore)
    }

}