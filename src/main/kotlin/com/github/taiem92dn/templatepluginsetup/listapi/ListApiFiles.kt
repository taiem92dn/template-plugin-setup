package com.github.taiem92dn.templatepluginsetup.listapi

import com.github.taiem92dn.templatepluginsetup.utils.lowercaseFirstLetter

fun dataSourceFile(
    packageName: String,
    itemName: String,
) = """package $packageName.data

import $packageName.model.${itemName}Item

interface ${itemName}DataSource {

    suspend fun get${itemName}List(): List<${itemName}Item>
} 
"""

fun remoteDataSourceFile(
    packageName: String,
    itemName: String,
) = """package $packageName.data

import $packageName.model.${itemName}Item
import $packageName.network.${itemName}Service

class Remote${itemName}DataSource constructor(val ${itemName.lowercaseFirstLetter()}Service: ${itemName}Service) : ${itemName}DataSource {
    override suspend fun get${itemName}List(): List<${itemName}Item> {
        val ${itemName.lowercaseFirstLetter()}List = ${itemName.lowercase()}Service.get${itemName}List().items
        return ${itemName.lowercaseFirstLetter()}List
    }
} 
""".trimIndent()

fun itemModelFile(
    packageName: String,
    itemName: String,
) = """package $packageName.model

data class ${itemName}Item(
)
""".trimIndent()

fun listResponseFile(
    packageName: String,
    itemName: String,
) = """package $packageName.model
    
import com.google.gson.annotations.SerializedName

data class ListResponse<T>(
    @SerializedName("items") val items: List<T> = emptyList(),
)
""".trimIndent()

fun retrofitServiceFile(
    packageName: String,
    itemName: String,
    urlPath: String,
) = """package $packageName.network

import $packageName.model.${itemName}Item
import retrofit2.http.GET
import $packageName.model.ListResponse

interface ${itemName}Service {

    @GET("$urlPath")
    suspend fun get${itemName}List(): ListResponse<${itemName}Item>
}
""".trimIndent()

fun repositoryFile(
    packageName: String,
    itemName: String,
) = """package $packageName.repository

import $packageName.data.${itemName}DataSource
import $packageName.model.${itemName}Item
import $packageName.network.ApiResource
import $packageName.network.INetworkCheckService
import $packageName.network.${itemName}Service
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ${itemName}Repository @Inject constructor(
    private val ${itemName.lowercaseFirstLetter()}DataSource: ${itemName}DataSource,
    private var networkCheckService: INetworkCheckService
) {
    private val TAG = ${itemName}Repository::class.java.simpleName

    suspend fun get${itemName}List(): ApiResource<List<${itemName}Item>> {
        if (!networkCheckService.hasInternet()) {
            return ApiResource.NoInternet(null)
        }

        return try {
            ApiResource.Success(${itemName.lowercaseFirstLetter()}DataSource.get${itemName}List())
        } catch (e: Throwable) {
            e.printStackTrace()
            ApiResource.Error(e.message)
        }
    }
} 
""".trimIndent()

fun recyclerViewAdapterFile(
    packageName: String,
    itemName: String,
) = """package $packageName.ui.${itemName.lowercaseFirstLetter()}list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import $packageName.databinding.Item${itemName}ListBinding
import $packageName.model.${itemName}Item

class ${itemName}ListAdapter: ListAdapter<${itemName}Item, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var onItemClickListener: (${itemName.lowercaseFirstLetter()}Item: ${itemName}Item) -> Unit? = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = Item${itemName}ListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ${itemName}ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ${itemName}ItemViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }


    inner class ${itemName}ItemViewHolder(val binding: Item${itemName}ListBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClickListener(getItem(bindingAdapterPosition))
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
                return oldItem.description == newItem.description
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
) = """package $packageName.ui.${itemName.lowercaseFirstLetter()}list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import $packageName.R
import $packageName.databinding.ActivityMainBinding
import $packageName.databinding.Fragment${itemName}ListBinding
import $packageName.model.${itemName}Item
import $packageName.network.ApiResource
import $packageName.ui.${itemName.lowercaseFirstLetter()}list.adapter.${itemName}ListAdapter
import dagger.hilt.android.AndroidEntryPoint
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
    private var isRefreshingData = false

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
        isRefreshingData = false
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
            refreshData()
        }

        adapter?.onItemClickListener = {
            navigateToDetail(it)
        }
    }

    private fun observeData() {
        viewModel.${itemName.lowercaseFirstLetter()}ListResponse.observe(viewLifecycleOwner) {
            showResultData(it)
            if ((it is ApiResource.Loading).not())
                isRefreshingData = false
        }
    }

    private fun navigateToDetail(${itemName.lowercaseFirstLetter()}Item: ${itemName}Item) {
//        val action =
//            ItemListFragmentDirections
//                .actionItemListFragmentToMovieDetailFragment(item)
//        findNavController().navigate(action)
    }

    private fun showResultData(apiResource: ApiResource<List<${itemName}Item>>) {
        hideLoading()
        hideNoInternet()
        viewModel.hideError()
        when (apiResource) {
            is ApiResource.Success -> {
                apiResource.data?.let {
                    showData(it)
                }
            }
            is ApiResource.Error -> {
                if (adapter?.itemCount == 0)
                    viewModel.setShowError(getString(R.string.unable_to_get_data))
                else
                    showSnackbarError(getString(R.string.unable_to_get_data))
            }
            is ApiResource.NoInternet -> {
                if (adapter?.itemCount == 0)
                    viewModel.setShowError(getString(R.string.no_internet))
                else
                    showSnackbarError(getString(R.string.no_internet))
            }
            is ApiResource.Loading -> {
                showLoading()
            }
        }
    }

    private fun initAdapter() {
        adapter = ${itemName}ListAdapter()
        binding.contentList.rvList.also {
            it.adapter = adapter
            it.setHasFixedSize(true)
        }
    }

    open fun loadListData() {
        viewModel.get${itemName}List()
    }

    private fun refreshData() {
        isRefreshingData = true
        loadListData()
    }

    private fun showData(data: List<${itemName}Item>) {
        adapter?.submitList(data)
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
            noInternetSnackbar = Snackbar.make(binding.root, getText(R.string.no_internet), Snackbar.LENGTH_INDEFINITE)
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
) = """package $packageName.ui.${itemName.lowercaseFirstLetter()}list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import $packageName.model.${itemName}Item
import $packageName.network.ApiResource
import $packageName.repository.${itemName}Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ${itemName}ListViewModel @Inject constructor(
    private val ${itemName.lowercaseFirstLetter()}Repository: ${itemName}Repository
) : ViewModel() {

    private var _${itemName.lowercaseFirstLetter()}ListResponse = MutableLiveData<ApiResource<List<${itemName}Item>>>()
    val ${itemName.lowercaseFirstLetter()}ListResponse: LiveData<ApiResource<List<${itemName}Item>>>
        get() = _${itemName.lowercaseFirstLetter()}ListResponse

    val errorMessage = MutableLiveData<String>()
    val showError = MutableLiveData<Boolean>(false)

    fun get${itemName}List() {
        viewModelScope.launch {
            _${itemName.lowercaseFirstLetter()}ListResponse.value = ApiResource.Loading()
            _${itemName.lowercaseFirstLetter()}ListResponse.value = ${itemName.lowercase()}Repository.get${itemName}List()
        }
    }

    fun setShowError(message: String) {
        errorMessage.value = message
        showError.value = true
    }

    fun hideError() {
        showError.value = false
    }

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