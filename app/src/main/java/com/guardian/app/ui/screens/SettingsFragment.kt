package com.guardian.app.ui.screens

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.guardian.app.R
import com.guardian.app.databinding.FragmentSettingsBinding
import com.guardian.app.viewmodel.GuardianViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: GuardianViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwitches()
        setupButtons()
        observeData()
    }
    
    private fun setupSwitches() {
        binding.switchProtection.setOnCheckedChangeListener { _, isChecked ->
            if (viewModel.isProtectionEnabled.value != isChecked) {
                viewModel.toggleProtection()
            }
        }
        
        binding.switchUsb.setOnCheckedChangeListener { _, isChecked ->
            if (viewModel.usbStatus.value != isChecked) {
                viewModel.toggleUsbStatus()
            }
        }
    }
    
    private fun setupButtons() {
        binding.resetStatsButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.reset_stats))
                .setMessage(getString(R.string.reset_stats_confirm))
                .setPositiveButton(getString(R.string.reset)) { _, _ ->
                    viewModel.resetStats()
                    Toast.makeText(context, getString(R.string.reset_complete), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isProtectionEnabled.collectLatest { enabled ->
                binding.switchProtection.isChecked = enabled
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.usbStatus.collectLatest { status ->
                binding.switchUsb.isChecked = status
                binding.usbStatusText.text = if (status) 
                    getString(R.string.usb_debugging_status) 
                else 
                    getString(R.string.usb_debugging_status_off)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
