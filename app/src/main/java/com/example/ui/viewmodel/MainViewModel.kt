package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DiagnosticTest
import com.example.data.repository.SyncStatus
import com.example.data.repository.TestRepository
import com.example.network.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TestRepository(application)
    private val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = networkMonitor.isCurrentlyConnected()
    )

    val syncStatus: StateFlow<SyncStatus> = repository.syncStatus

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow("name_asc")
    val sortBy = _sortBy.asStateFlow()

    val tests: StateFlow<List<DiagnosticTest>> = combine(
        _searchQuery,
        _sortBy
    ) { query, sort ->
        Pair(query, sort)
    }.flatMapLatest { (query, sort) ->
        repository.getAllTests(query, sort)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected tests in the calculation tray: map of ID -> Test
    private val _selectedTests = MutableStateFlow<Map<String, DiagnosticTest>>(emptyMap())
    val selectedTests = _selectedTests.asStateFlow()

    // Discount percentage (0, 5, 10, 15, 20 etc)
    private val _discountPercentage = MutableStateFlow(0.0)
    val discountPercentage = _discountPercentage.asStateFlow()

    // Dialog & Sheet States
    private val _showEstimateSheet = MutableStateFlow(false)
    val showEstimateSheet = _showEstimateSheet.asStateFlow()

    private val _showCreditsDialog = MutableStateFlow(false)
    val showCreditsDialog = _showCreditsDialog.asStateFlow()

    private val _showSyncSettingsDialog = MutableStateFlow(false)
    val showSyncSettingsDialog = _showSyncSettingsDialog.asStateFlow()

    private var previousOnlineState: Boolean? = null

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            if (isOnline.value) {
                repository.syncFromRemote(isAutoSync = true)
            }
        }

        // Automatic update when network becomes available
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online && previousOnlineState == false) {
                    repository.syncFromRemote(isAutoSync = true)
                }
                previousOnlineState = online
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortByChange(sort: String) {
        _sortBy.value = sort
    }

    fun toggleTestSelection(test: DiagnosticTest) {
        val current = _selectedTests.value.toMutableMap()
        val key = test.effectiveId
        if (current.containsKey(key)) {
            current.remove(key)
        } else {
            current[key] = test
        }
        _selectedTests.value = current
    }

    fun isTestSelected(testId: String): Boolean {
        return _selectedTests.value.containsKey(testId)
    }

    fun removeTestFromSelection(testId: String) {
        val current = _selectedTests.value.toMutableMap()
        current.remove(testId)
        _selectedTests.value = current
    }

    fun clearAllSelections() {
        _selectedTests.value = emptyMap()
    }

    fun setDiscount(percent: Double) {
        _discountPercentage.value = percent
    }

    fun setEstimateSheetVisible(visible: Boolean) {
        _showEstimateSheet.value = visible
    }

    fun setCreditsDialogVisible(visible: Boolean) {
        _showCreditsDialog.value = visible
    }

    fun setSyncSettingsDialogVisible(visible: Boolean) {
        _showSyncSettingsDialog.value = visible
    }

    fun manualSync() {
        viewModelScope.launch {
            repository.syncFromRemote(isAutoSync = false)
        }
    }

    fun resetCatalogToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaultCatalog()
        }
    }

    fun saveSupabaseConfig(url: String, key: String, table: String) {
        repository.supabaseService.saveConfig(url, key, table)
        viewModelScope.launch {
            repository.syncFromRemote(isAutoSync = false)
        }
    }

    fun getSupabaseConfig(): Triple<String, String, String> {
        val s = repository.supabaseService
        return Triple(s.getSupabaseUrl(), s.getSupabaseKey(), s.getSupabaseTable())
    }

    fun getLastSyncTimeString(): String {
        val time = repository.supabaseService.getLastSyncTime()
        if (time == 0L) return "Never synced"
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }

    fun generateEstimateSummaryText(): String {
        val items = _selectedTests.value.values.toList()
        if (items.isEmpty()) return ""

        val subtotal = items.sumOf { it.price }
        val discountPercent = _discountPercentage.value
        val discountAmount = subtotal * (discountPercent / 100.0)
        val finalPrice = subtotal - discountAmount
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

        val sb = StringBuilder()
        sb.append("════════════════════════════════\n")
        sb.append(" NEW SHAFIPUR GENERAL HOSPITAL \n")
        sb.append(" Shafipur, Kaliakair, Gazipur   \n")
        sb.append(" Diagnostic Test Price Estimate \n")
        sb.append(" Date: $dateStr\n")
        sb.append("════════════════════════════════\n\n")

        items.forEachIndexed { index, test ->
            sb.append("${index + 1}. ${test.name}\n")
            sb.append("   Price: ৳ ${test.price.toInt()}\n")
        }

        sb.append("\n────────────────────────────────\n")
        sb.append("Total Tests: ${items.size}\n")
        sb.append("Subtotal: ৳ ${subtotal.toInt()}\n")
        if (discountPercent > 0) {
            sb.append("Discount ($discountPercent%): - ৳ ${discountAmount.toInt()}\n")
        }
        sb.append("Estimated Net Total: ৳ ${finalPrice.toInt()}\n")
        sb.append("────────────────────────────────\n")
        sb.append("⚠️ NOTE: Please Confirm The Final Price From The Counter.. 01711902062\n")
        sb.append("App Developed by: Hridoy Islam Rabby (01780960370)\n")
        sb.append("IT-Incharge, New Shafipur General Hospital\n")
        sb.append("════════════════════════════════\n")

        return sb.toString()
    }
}
