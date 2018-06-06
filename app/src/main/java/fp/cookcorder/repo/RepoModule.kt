package fp.cookcorder.repo

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Singleton
@Module
abstract class RepoModule {

    @Singleton
    @Binds
    abstract fun bindTaskRepo(impl: TaskRepoImpl): TaskRepo

}