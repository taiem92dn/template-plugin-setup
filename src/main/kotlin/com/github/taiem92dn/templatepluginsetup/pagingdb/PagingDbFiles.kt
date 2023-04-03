package com.github.taiem92dn.templatepluginsetup.pagingdb

import com.github.taiem92dn.templatepluginsetup.utils.lowercaseFirstLetter

fun repositoryFile(
    packageName: String,
    itemName: String,
    databaseName: String,
) = """package ${packageName}.repository

import android.app.Application
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ${packageName}.data.${itemName}DataSource
import ${packageName}.data.${itemName}ItemRemoteMediator
import ${packageName}.db.${databaseName}Database
import ${packageName}.model.${itemName}Item
import ${packageName}.network.INetworkCheckService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ${itemName}Repository @Inject constructor(
    private val ${itemName.lowercaseFirstLetter()}DataSource: ${itemName}DataSource,
    private val database: ${databaseName}Database,
    private val networkCheckService: INetworkCheckService,
    private val application: Application
) {
    private val TAG = ${itemName}Repository::class.java.simpleName

    fun get${itemName}ResultStream(query: String): Flow<PagingData<${itemName}Item>> {
        // appending '%' so we can allow other characters to be before and after the query string
//        val dbQuery = "%${'$'}{query.replace(' ', '%')}%"
        val pagingSourceFactory =  { database.${itemName.lowercaseFirstLetter()}Dao().${itemName.lowercaseFirstLetter()}sByName()}

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            remoteMediator = ${itemName}ItemRemoteMediator(
                query,
                ${itemName.lowercaseFirstLetter()}DataSource,
                database,
                application,
                networkCheckService
            ),
            pagingSourceFactory = pagingSourceFactory

        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 30
    }
} 
""".trimIndent()

fun itemModelDbFile(
    packageName: String,
    itemName: String,
) = """package $packageName.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "${itemName.lowercase()}s")
data class ${itemName}Item(
    @PrimaryKey @field:SerializedName("id") val id: Long,
)
""".trimIndent()

fun remoteMediatorFile(
    packageName: String,
    itemName: String,
    databaseName: String
) = """package ${packageName}.data

import android.app.Application
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ${packageName}.R
import ${packageName}.db.Remote${itemName}Keys
import ${packageName}.db.${databaseName}Database
import ${packageName}.model.${itemName}Item
import ${packageName}.network.IN_QUALIFIER
import ${packageName}.network.INetworkCheckService
import retrofit2.HttpException
import java.io.IOException

private const val GITHUB_STARTING_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class ${itemName}ItemRemoteMediator(
    private val query: String,
    private val service: ${itemName}DataSource,
    private val ${itemName.lowercaseFirstLetter()}Database: ${databaseName}Database,
    private val application: Application,
    private val networkCheckService: INetworkCheckService
) : RemoteMediator<Int, ${itemName}Item>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, ${itemName}Item>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: GITHUB_STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with endOfPaginationReached = false because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                // the end of pagination for append.
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        val apiQuery = query + IN_QUALIFIER

        try {
            if (!networkCheckService.hasInternet())
                return MediatorResult.Error(
                    Exception(application.getString(R.string.no_internet))
                )

            val apiResponse = service.get${itemName}List(apiQuery, page, state.config.pageSize)

            val items = apiResponse
            val endOfPaginationReached = items.isEmpty()
            ${itemName.lowercaseFirstLetter()}Database.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    ${itemName.lowercaseFirstLetter()}Database.remote${itemName}KeysDao().clearRemoteKeys()
                    ${itemName.lowercaseFirstLetter()}Database.${itemName.lowercaseFirstLetter()}Dao().clear${itemName}s()
                }
                val prevKey = if (page == GITHUB_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = items.map {
                    Remote${itemName}Keys(${itemName.lowercaseFirstLetter()}Id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                ${itemName.lowercaseFirstLetter()}Database.remote${itemName}KeysDao().insertAll(keys)
                ${itemName.lowercaseFirstLetter()}Database.${itemName.lowercaseFirstLetter()}Dao().insertAll(items)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ${itemName}Item>): Remote${itemName}Keys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { item ->
                // Get the remote keys of the last item retrieved
                ${itemName.lowercaseFirstLetter()}Database.remote${itemName}KeysDao().remote${itemName}KeysId(item.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ${itemName}Item>): Remote${itemName}Keys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { item ->
                // Get the remote keys of the first items retrieved
                ${itemName.lowercaseFirstLetter()}Database.remote${itemName}KeysDao().remote${itemName}KeysId(item.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, ${itemName}Item>
    ): Remote${itemName}Keys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { itemId ->
                ${itemName.lowercaseFirstLetter()}Database.remote${itemName}KeysDao().remote${itemName}KeysId(itemId)
            }
        }
    }
}
""".trimIndent()


fun pagingDbFragmentFile(
    packageName: String,
    itemName: String,
) = """
package ${packageName}.ui.${itemName.lowercase()}list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.google.android.material.snackbar.Snackbar
import ${packageName}.R
import ${packageName}.databinding.Fragment${itemName}ListBinding
import ${packageName}.model.${itemName}Item
import ${packageName}.ui.${itemName.lowercase()}list.adapter.${itemName}ListAdapter
import ${packageName}.ui.${itemName.lowercase()}list.adapter.ItemsLoadStateAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
open class ${itemName}ListFragment : Fragment() {

    companion object {
        fun newInstance() = ${itemName}ListFragment()
    }

    protected val viewModel by lazy { ViewModelProvider(this)[${itemName}ListViewModel::class.java] }

    private var _binding: Fragment${itemName}ListBinding? = null
    private val binding get() = _binding!!

    private var adapter: ${itemName}ListAdapter? = null
    private var noInternetSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Fragment${itemName}ListBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated")

        initAdapter()
        observeData()
        bindEvents()
        bindData()

        refreshData()
    }

    private fun bindData() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            showError = viewModel.showError
            errorMessage = viewModel.errorMessage
        }
    }

    private fun bindEvents() {

        binding.contentList.srlList.setOnRefreshListener {
            refreshData()
        }

        binding.contentList.btRetry.setOnClickListener {
            viewModel.hideError()
            adapter?.retry()
        }

        adapter?.onItemClickListener = {
            navigateToDetail(it)
        }
    }

    private fun observeData() {
    }

    private fun navigateToDetail(${itemName.lowercaseFirstLetter()}Item: ${itemName}Item) {
//        val action =
//            ItemListFragmentDirections
//                .actionItemListFragmentToMovieDetailFragment(item)
//        findNavController().navigate(action)
    }

    private fun initAdapter() {
        adapter = ${itemName}ListAdapter()

        val header = ItemsLoadStateAdapter { adapter?.retry() }
        binding.contentList.rvList.also {
            it.adapter = adapter?.withLoadStateHeaderAndFooter(
                header = header,
                footer = ItemsLoadStateAdapter { adapter?.retry() }
            )
            it.setHasFixedSize(true)
        }
        binding.bindList(header, adapter!!, viewModel.pagingDataFlow)
    }

    private fun Fragment${itemName}ListBinding.bindList(
        header: ItemsLoadStateAdapter,
        ${itemName.lowercaseFirstLetter()}Adapter: ${itemName}ListAdapter,
        pagingData: Flow<PagingData<${itemName}Item>>,
    ) {
        lifecycleScope.launch {
            pagingData.collectLatest(${itemName.lowercaseFirstLetter()}Adapter::submitData)
        }

        lifecycleScope.launch {
            ${itemName.lowercaseFirstLetter()}Adapter.loadStateFlow.collect { loadState ->
                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading
                            && loadState.source.refresh is LoadState.NotLoading
                            && loadState.mediator?.refresh is LoadState.NotLoading
                            && ${itemName.lowercaseFirstLetter()}Adapter.itemCount == 0
                // show empty list
                if (isListEmpty) {
                    viewModel.setShowError(getString(R.string.no_results))
                }
                else {
                    viewModel.hideError()
                }

                // Only show the list if refresh succeeds, either from the the local db or the remote.
                contentList.rvList.isVisible =  loadState.source.refresh is LoadState.NotLoading
                        || loadState.mediator?.refresh is LoadState.NotLoading
                // Show loading spinner during initial load or refresh.
                if (loadState.mediator?.refresh is LoadState.Loading) {
                    showLoading()
                } else {
                    hideLoading()
                }

                // Show the retry state if initial load or refresh fails.
                if (loadState.mediator?.refresh is LoadState.Error
                    && ${itemName.lowercaseFirstLetter()}Adapter.itemCount == 0
                ) {
                    viewModel.setShowError(
                        (loadState.mediator?.refresh as LoadState.Error).error.message ?: ""
                    )
                }

                // Show a retry header if there was an error refreshing, and items were previously
                // cached OR default to the default prepend state
                header.loadState = loadState.mediator
                    ?.refresh
                    ?.takeIf { it is LoadState.Error && ${itemName.lowercaseFirstLetter()}Adapter.itemCount > 0 }
                    ?: loadState.prepend


                // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                    ?: loadState.source.refresh as? LoadState.Error
                    ?: loadState.mediator?.refresh as? LoadState.Error

                errorState?.let {
                    Toast.makeText(
                        requireContext(),
                        "\uD83D\uDE28 Wooops ${'$'}{it.error}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun refreshData() {
        adapter?.refresh()
    }

    private fun showLoading() {
        binding.contentList.srlList.isRefreshing = true
    }

    private fun hideLoading() {
        binding.contentList.srlList.isRefreshing = false
    }

    private fun showSnackbarError(message: CharSequence) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showNoInternet() {
        if (noInternetSnackbar == null)
            noInternetSnackbar = Snackbar.make(
                binding.root,
                getText(R.string.no_internet),
                Snackbar.LENGTH_INDEFINITE
            )
        noInternetSnackbar?.show()
    }

    private fun hideNoInternet() {
        noInternetSnackbar?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
""".trimIndent()

fun remoteKeysDaoFile(
    packageName: String,
    itemName: String,
) = """package ${packageName}.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface Remote${itemName}KeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<Remote${itemName}Keys>)

    @Query("SELECT * FROM remote_${itemName.lowercase()}_keys WHERE ${itemName.lowercaseFirstLetter()}Id = :${itemName.lowercaseFirstLetter()}Id")
    suspend fun remote${itemName}KeysId(${itemName.lowercaseFirstLetter()}Id: Long): Remote${itemName}Keys?

    @Query("DELETE FROM remote_${itemName.lowercase()}_keys")
    suspend fun clearRemoteKeys()
}
"""

fun remoteKeyModelFile(
    packageName: String,
    itemName: String,
) = """package ${packageName}.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_${itemName.lowercaseFirstLetter()}_keys")
data class Remote${itemName}Keys(
    @PrimaryKey
    val ${itemName.lowercaseFirstLetter()}Id: Long,
    val prevKey: Int?,
    val nextKey: Int?
)
""".trimIndent()

fun itemDbDaoFile(
    packageName: String,
    itemName: String,
) = """package ${packageName}.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ${packageName}.model.${itemName}Item

@Dao
interface ${itemName}Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<${itemName}Item>)

    @Query("SELECT * FROM ${itemName.lowercase()}s " 
//            +
//            "WHERE fullName LIKE :queryString " +
//            "ORDER BY stars DESC, fullName ASC"
    )
    fun ${itemName.lowercaseFirstLetter()}sByName(): PagingSource<Int, ${itemName}Item>

    @Query("DELETE FROM ${itemName.lowercase()}s")
    suspend fun clear${itemName}s()
}
""".trimIndent()

fun pagingDatabaseFile(
    packageName: String,
    itemName: String,
    databaseName: String,
) = """package ${packageName}.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ${packageName}.model.${itemName}Item

@Database(
    entities = [${itemName}Item::class, Remote${itemName}Keys::class],
    version = 1,
    exportSchema = false
)
abstract class ${databaseName}Database : RoomDatabase() {

    abstract fun ${itemName.lowercaseFirstLetter()}Dao(): ${itemName}Dao
    abstract fun remote${itemName}KeysDao(): Remote${itemName}KeysDao


    companion object {

        @Volatile
        private var INSTANCE: ${databaseName}Database? = null

        fun getInstance(context: Context): ${databaseName}Database =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                ${databaseName}Database::class.java, "$databaseName.db")
                .build()
    }
}
""".trimIndent()

fun addProviderInAppModule(
    packageName: String,
    itemName: String,
    databaseName: String
) = """
    
        @Provides
        @Singleton
        fun provideDatabase(application: Application) = ${databaseName}Database.getInstance(application)
""".trimIndent()