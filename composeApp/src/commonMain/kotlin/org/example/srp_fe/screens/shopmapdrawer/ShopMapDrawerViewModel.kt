package org.example.srp_fe.screens.shopmapdrawer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.ApiRepository
import org.openapitools.client.models.Store
import org.openapitools.client.models.WallBlock
import org.openapitools.client.models.Map


class ShopMapDrawerViewModel(val apiRepository: ApiRepository) : ViewModel() {

	private val _map = mutableStateOf<Map?>(null)
	val map: State<Map?> = _map

	private val _departments = mutableStateOf<List<Department>>(emptyList())
	val departments: State<List<Department>> = _departments

	private val _stores = mutableStateOf<List<Store>>(emptyList())
	val stores: State<List<Store>> = _stores

	private val _wallBlocks = mutableStateOf<List<WallBlock>>(emptyList())
	val wallBlocks: State<List<WallBlock>> = _wallBlocks

	fun fetchMapById(id: Int) {
		viewModelScope.launch {
			apiRepository.getMapById(id)?.let {
				_map.value = it
			}
		}
	}
}