package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DiagnosticTest
import com.example.data.model.Doctor
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

import androidx.compose.runtime.Immutable

@Immutable
enum class AppScreen {
    TESTS,
    DOCTORS
}

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
    val noticeText: StateFlow<String> = repository.noticeText

    // Navigation screen state
    private val _currentScreen = MutableStateFlow(AppScreen.TESTS)
    val currentScreen = _currentScreen.asStateFlow()

    // Slide-in Drawer / Side Sheet state
    private val _showSideDrawer = MutableStateFlow(false)
    val showSideDrawer = _showSideDrawer.asStateFlow()

    // Search & Sort for Tests
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

    // Doctor List state & filtering
    private val _doctorSearchQuery = MutableStateFlow("")
    val doctorSearchQuery = _doctorSearchQuery.asStateFlow()

    private val _selectedSpecialityFilter = MutableStateFlow("All")
    val selectedSpecialityFilter = _selectedSpecialityFilter.asStateFlow()

    val allDoctors: StateFlow<List<Doctor>> = repository.getAllDoctors().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredDoctors: StateFlow<List<Doctor>> = combine(
        allDoctors,
        _doctorSearchQuery,
        _selectedSpecialityFilter
    ) { docs, query, spec ->
        docs.filter { doc ->
            val matchesQuery = query.isBlank() ||
                doc.name.contains(query, ignoreCase = true) ||
                doc.speciality.contains(query, ignoreCase = true) ||
                doc.degree.contains(query, ignoreCase = true) ||
                doc.chamberDay.contains(query, ignoreCase = true) ||
                doc.chamberTime.contains(query, ignoreCase = true) ||
                doc.roomNo.contains(query, ignoreCase = true) ||
                doc.chamberDay2.contains(query, ignoreCase = true) ||
                doc.chamberTime2.contains(query, ignoreCase = true) ||
                doc.roomNo2.contains(query, ignoreCase = true)

            val matchesSpec = spec == "All" || doc.speciality.contains(spec, ignoreCase = true)

            matchesQuery && matchesSpec
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected tests in calculation tray
    private val _selectedTests = MutableStateFlow<Map<String, DiagnosticTest>>(emptyMap())
    val selectedTests = _selectedTests.asStateFlow()

    // Discount percentage
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

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSideDrawerVisible(visible: Boolean) {
        _showSideDrawer.value = visible
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortByChange(sort: String) {
        _sortBy.value = sort
    }

    fun onDoctorSearchChange(query: String) {
        _doctorSearchQuery.value = query
    }

    fun onSpecialityFilterChange(speciality: String) {
        _selectedSpecialityFilter.value = speciality
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

    fun setDiscount(percentage: Double) {
        _discountPercentage.value = percentage
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

    fun saveSupabaseConfig(url: String, key: String, table: String) {
        repository.supabaseService.saveConfig(url, key, table)
        manualSync()
    }

    fun getSupabaseConfig(): Triple<String, String, String> {
        return Triple(
            repository.supabaseService.getSupabaseUrl(),
            repository.supabaseService.getSupabaseKey(),
            repository.supabaseService.getSupabaseTable()
        )
    }

    fun getLastSyncTimeString(): String {
        val time = repository.supabaseService.getLastSyncTime()
        if (time == 0L) return "Never synced"
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }

    fun resetCatalogToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaultCatalog()
        }
    }

    fun generateEstimateSummaryText(): String {
        val list = _selectedTests.value.values.toList()
        if (list.isEmpty()) return ""

        val sb = StringBuilder()
        sb.appendLine("🏥 NEW SHAFIPUR GENERAL HOSPITAL (NSGH)")
        sb.appendLine("📋 Diagnostic Test Bill Estimate")
        sb.appendLine("📅 Date: ${SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date())}")
        sb.appendLine("--------------------------------")

        var subtotal = 0.0
        list.forEachIndexed { index, test ->
            sb.appendLine("${index + 1}. ${test.name} - ৳ ${test.price.toInt()}")
            subtotal += test.price
        }

        sb.appendLine("--------------------------------")
        sb.appendLine("Subtotal (${list.size} tests): ৳ ${subtotal.toInt()}")

        val discount = _discountPercentage.value
        if (discount > 0) {
            val discountAmount = subtotal * (discount / 100.0)
            sb.appendLine("Special Discount (${discount.toInt()}%): - ৳ ${discountAmount.toInt()}")
            sb.appendLine("Total Payable: ৳ ${(subtotal - discountAmount).toInt()}")
        } else {
            sb.appendLine("Total Payable: ৳ ${subtotal.toInt()}")
        }

        sb.appendLine("--------------------------------")
        sb.appendLine("⚠️ Please confirm the final price at the cash counter.")
        sb.appendLine("📞 Helpline: 01711902062")
        return sb.toString()
    }
}
