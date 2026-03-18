package com.guardian.app.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.guardian.app.databinding.FragmentLogBinding
import com.guardian.app.viewmodel.GuardianViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LogFragment : Fragment() {
    
    private var _binding: FragmentLogBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: GuardianViewModel by activityViewModels()
    private lateinit var adapter: LogAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
    }
    
    private fun setupRecyclerView() {
        adapter = LogAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.logs.collectLatest { logs ->
                adapter.submitList(logs)
                binding.emptyView.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
