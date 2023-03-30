package com.github.taiem92dn.templatepluginsetup.paging

import com.github.taiem92dn.templatepluginsetup.utils.lowercaseFirstLetter

fun dataSourceFile(
    packageName: String,
    itemName: String,
) = """package $packageName.data

import $packageName.model.${itemName}Item

interface ${itemName}DataSource {

    suspend fun get${itemName}List(
        query: String,
        page: Int,
        itemsPerPage: Int
    ): List<${itemName}Item>
} 
"""

fun remoteDataSourceFile(
    packageName: String,
    itemName: String,
) = """package $packageName.data

import $packageName.model.${itemName}Item
import $packageName.network.${itemName}Service

class Remote${itemName}DataSource constructor(val ${itemName.lowercaseFirstLetter()}Service: ${itemName}Service) : ${itemName}DataSource {
    override suspend fun get${itemName}List(
        query: String,
        page: Int,
        itemsPerPage: Int
    ): List<${itemName}Item> {
        val ${itemName.lowercaseFirstLetter()}List = 
                ${itemName.lowercase()}Service.get${itemName}List(query, page, itemsPerPage).items
        return ${itemName.lowercaseFirstLetter()}List
    }
} 
""".trimIndent()

fun retrofitServiceFile(
    packageName: String,
    itemName: String,
    urlPath: String,
) = """package $packageName.network

import $packageName.model.${itemName}Item
import retrofit2.http.GET
import retrofit2.http.Query
import $packageName.model.ListResponse

const val IN_QUALIFIER = "in:name,description"

interface ${itemName}Service {

    @GET("$urlPath")
    suspend fun get${itemName}List(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") itemsPerPage: Int
    ): ListResponse<${itemName}Item>
}
""".trimIndent()

fun repositoryFile(
    packageName: String,
    itemName: String,
) = """package $packageName.repository

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import $packageName.data.${itemName}DataSource
import $packageName.data.${itemName}PagingSource
import $packageName.model.${itemName}Item
import $packageName.network.ApiResource
import $packageName.network.INetworkCheckService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ${itemName}Repository @Inject constructor(
    private val ${itemName.lowercaseFirstLetter()}DataSource: ${itemName}DataSource,
    private val networkCheckService: INetworkCheckService,
    private val application: Application
) {
    private val TAG = ${itemName}Repository::class.java.simpleName

    fun get${itemName}ResultStream(query: String): Flow<PagingData<${itemName}Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ${itemName}PagingSource(${itemName.lowercaseFirstLetter()}DataSource, query, application, networkCheckService)
            }
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 30
    }
} 
""".trimIndent()

fun pagingSourceFile(
    packageName: String,
    itemName: String,
) = """package ${packageName}.data

import android.app.Application
import androidx.paging.PagingSource
import androidx.paging.PagingState
import ${packageName}.R
import ${packageName}.model.${itemName}Item
import ${packageName}.network.IN_QUALIFIER
import ${packageName}.network.INetworkCheckService
import ${packageName}.repository.${itemName}Repository.Companion.NETWORK_PAGE_SIZE
import retrofit2.HttpException
import java.io.IOException

// GitHub page API is 1 based: https://developer.github.com/v3/#pagination
private const val STARTING_PAGE_INDEX = 1

class ${itemName}PagingSource(
    private val dataSource: ${itemName}DataSource,
    private val query: String,
    private val application: Application,
    private val networkCheckService: INetworkCheckService
) : PagingSource<Int, ${itemName}Item>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ${itemName}Item> {
        val position = params.key ?: STARTING_PAGE_INDEX
        val apiQuery = query + IN_QUALIFIER

        if (!networkCheckService.hasInternet())
            return LoadResult.Error(Exception(application.getString(R.string.no_internet)))

        return try {
            val response = dataSource.get${itemName}List(apiQuery, position, params.loadSize)
            val ${itemName.lowercaseFirstLetter()}List = response
            val nextKey = if (${itemName.lowercaseFirstLetter()}List.isEmpty()) {
                null
            } else {
                // initial load size = 3 * NETWORK_PAGE_SIZE
                // ensure we're not requesting duplicating items, at the 2nd request
                position + (params.loadSize / NETWORK_PAGE_SIZE)
            }
            LoadResult.Page(
                data = ${itemName.lowercaseFirstLetter()}List,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
    // The refresh key is used for subsequent refresh calls to PagingSource.load after the initial load
    override fun getRefreshKey(state: PagingState<Int, ${itemName}Item>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}
""".trimIndent()

fun recyclerViewAdapterFile(
    packageName: String,
    itemName: String,
) = """package ${packageName}.ui.${itemName.lowercase()}list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ${packageName}.databinding.Item${itemName}ListBinding
import ${packageName}.model.${itemName}Item

class ${itemName}ListAdapter : PagingDataAdapter<${itemName}Item, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var onItemClickListener: (${itemName.lowercaseFirstLetter()}Item: ${itemName}Item) -> Unit? = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            Item${itemName}ListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ${itemName}ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ${itemName}ItemViewHolder -> {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    inner class ${itemName}ItemViewHolder(val binding: Item${itemName}ListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                getItem(bindingAdapterPosition)?.let { item -> onItemClickListener(item) }
            }
        }

        fun bind(item: ${itemName}Item) {
            binding.also {
                it.${itemName.lowercaseFirstLetter()}Item = item
                it.executePendingBindings()
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<${itemName}Item>() {
            override fun areItemsTheSame(
                oldItem: ${itemName}Item,
                newItem: ${itemName}Item
            ): Boolean {
                return oldItem.fullName == newItem.fullName
            }

            override fun areContentsTheSame(
                oldItem: ${itemName}Item,
                newItem: ${itemName}Item
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}   
""".trimIndent()

fun listFragmentFile(
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
        binding.contentList.rvList.also {
            it.adapter = adapter?.withLoadStateHeaderAndFooter(
                header = ItemsLoadStateAdapter { adapter?.retry() },
                footer = ItemsLoadStateAdapter { adapter?.retry() }
            )
            it.setHasFixedSize(true)
        }
        binding.bindList(adapter!!, viewModel.pagingDataFlow)
    }

    private fun Fragment${itemName}ListBinding.bindList(
        ${itemName.lowercaseFirstLetter()}Adapter: ${itemName}ListAdapter,
        pagingData: Flow<PagingData<${itemName}Item>>,
    ) {
        lifecycleScope.launch {
            pagingData.collectLatest(${itemName.lowercaseFirstLetter()}Adapter::submitData)
        }

        lifecycleScope.launch {
            ${itemName.lowercaseFirstLetter()}Adapter.loadStateFlow.collect { loadState ->
                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && ${itemName.lowercaseFirstLetter()}Adapter.itemCount == 0
                // show empty list
                if (isListEmpty) {
                    viewModel.setShowError(getString(R.string.no_results))
                }
                // Only show the list if refresh succeeds.
                contentList.rvList.isVisible = !isListEmpty
                // Show loading spinner during initial load or refresh.
                if (loadState.source.refresh is LoadState.Loading) {
                    showLoading()
                }
                else {
                    hideLoading()
                }

                // Show the retry state if initial load or refresh fails.
                if (loadState.source.refresh is LoadState.Error
                    && ${itemName.lowercaseFirstLetter()}Adapter.itemCount == 0) {
                    viewModel.setShowError(
                        (loadState.source.refresh as LoadState.Error).error.message ?: ""
                    )
                }

                // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                    ?: loadState.source.refresh as? LoadState.Error

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

fun viewModelList(
    packageName: String,
    itemName: String,
) = """package ${packageName}.ui.${itemName.lowercase()}list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import ${packageName}.model.${itemName}Item
import ${packageName}.repository.${itemName}Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ${itemName}ListViewModel @Inject constructor(
    private val ${itemName.lowercaseFirstLetter()}Repository: ${itemName}Repository
) : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    val showError = MutableLiveData<Boolean>(false)

    val pagingDataFlow: Flow<PagingData<${itemName}Item>>

    init {
        pagingDataFlow = get${itemName}List()
    }

    fun setShowError(message: String) {
        errorMessage.value = message
        showError.value = true
    }

    fun hideError() {
        showError.value = false
    }

    private fun get${itemName}List(): Flow<PagingData<${itemName}Item>> =
        ${itemName.lowercaseFirstLetter()}Repository.get${itemName}ResultStream("chat")

    companion object {
        val TAG = ${itemName}ListViewModel::class.simpleName
    }
}
""".trimIndent()

fun addDependenciesInNetworkModule(
    packageName: String,
    itemName: String,
) = """
    
    @Provides
    @Singleton
    fun provide${itemName}DataSource(${itemName.lowercaseFirstLetter()}Service: ${itemName}Service): ${itemName}DataSource {
        return Remote${itemName}DataSource(${itemName.lowercaseFirstLetter()}Service)
    }

    @Provides
    @Singleton
    fun provide${itemName}Service(@Named("non_auth_retrofit") retrofit: Retrofit): ${itemName}Service {
        return retrofit.create(${itemName}Service::class.java)
    }
"""

fun itemLoadStateAdapter(
    packageName: String,
    itemName: String
) = """package ${packageName}.ui.${itemName.lowercase()}list.adapter

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class ItemsLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<ItemsLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: ItemsLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ItemsLoadStateViewHolder {
        return ItemsLoadStateViewHolder.create(parent, retry)
    }
} 
""".trimIndent()

fun itemLoadStateViewHolder(
    packageName: String,
    itemName: String
) = """package ${packageName}.ui.${itemName.lowercase()}list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import ${packageName}.R
import ${packageName}.databinding.ListLoadStateFooterViewItemBinding

class ItemsLoadStateViewHolder(
    private val binding: ListLoadStateFooterViewItemBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
        }
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState is LoadState.Error
        binding.errorMsg.isVisible = loadState is LoadState.Error
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): ItemsLoadStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_load_state_footer_view_item, parent, false)
            val binding = ListLoadStateFooterViewItemBinding.bind(view)
            return ItemsLoadStateViewHolder(binding, retry)
        }
    }
} 
""".trimIndent()