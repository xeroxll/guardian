package com.guardian.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guardian.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GuardianViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = Repository(application)
    
    // State
    private val _isProtectionEnabled = MutableStateFlow(true)
    val isProtectionEnabled: StateFlow<Boolean> = _isProtectionEnabled.asStateFlow()
    
    private val _usbStatus = MutableStateFlow(false)
    val usbStatus: StateFlow<Boolean> = _usbStatus.asStateFlow()
    
    private val _blacklist = MutableStateFlow<List<BlacklistedApp>>(emptyList())
    val blacklist: StateFlow<List<BlacklistedApp>> = _blacklist.asStateFlow()
    
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
    
    private val _stats = MutableStateFlow(Stats())
    val stats: StateFlow<Stats> = _stats.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.protectionEnabled.collect { _isProtectionEnabled.value = it }
        }
        viewModelScope.launch {
            repository.usbStatus.collect { _usbStatus.value = it }
        }
        viewModelScope.launch {
            repository.blacklist.collect { _blacklist.value = it }
        }
        viewModelScope.launch {
            repository.logs.collect { _logs.value = it }
        }
        viewModelScope.launch {
            repository.stats.collect { _stats.value = it }
        }
    }
    
    fun toggleProtection() {
        viewModelScope.launch {
            val newValue = !_isProtectionEnabled.value
            _isProtectionEnabled.value = newValue
            repository.setProtectionEnabled(newValue)
            
            if (newValue) {
                repository.addLog(LogType.CHECK, "Защита включена", "Активный мониторинг запущен")
            } else {
                repository.addLog(LogType.CHECK, "Защита выключена", "Устройство уязвимо")
            }
        }
    }
    
    fun toggleUsbStatus() {
        viewModelScope.launch {
            val newValue = !_usbStatus.value
            _usbStatus.value = newValue
            repository.setUsbStatus(newValue)
            
            if (newValue) {
                repository.addLog(LogType.THREAT, "USB отладка включена", "Обнаружена угроза")
            }
        }
    }
    
    fun addToBlacklist(name: String, packageName: String) {
        viewModelScope.launch {
            val app = BlacklistedApp(name = name, packageName = packageName)
            repository.addToBlacklist(app)
            repository.addLog(LogType.BLOCK, "Приложение заблокировано", name)
        }
    }
    
    fun removeFromBlacklist(id: String) {
        viewModelScope.launch {
            repository.removeFromBlacklist(id)
        }
    }
    
    fun startScan() {
        if (_isScanning.value) return
        
        viewModelScope.launch {
            _isScanning.value = true
            
            // Simulate scanning
            kotlinx.coroutines.delay(2000)
            
            val currentStats = _stats.value
            val newStats = currentStats.copy(
                checks = currentStats.checks + 1,
                threats = if (_usbStatus.value) currentStats.threats + 1 else currentStats.threats
            )
            _stats.value = newStats
            repository.updateStats { newStats }
            
            repository.addLog(LogType.CHECK, "Сканирование завершено", "Статус обновлен")
            
            _isScanning.value = false
        }
    }
    
    fun resetStats() {
        viewModelScope.launch {
            repository.resetStats()
            _stats.value = Stats()
        }
    }
}
