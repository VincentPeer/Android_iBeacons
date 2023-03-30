package ch.heigvd.iict.dma.labo2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import ch.heigvd.iict.dma.labo2.models.PersistentBeacon


class BeaconsViewModel : ViewModel() {

    private val _nearbyBeacons = MutableLiveData(mutableListOf<PersistentBeacon>())
    val nearbyBeacons : LiveData<List<PersistentBeacon>> = _nearbyBeacons.map { l -> l.toList().map { el -> el.copy() } }

    private val _closestBeacon = MutableLiveData<PersistentBeacon?>(null)
    val closestBeacon : LiveData<PersistentBeacon?> get() = _closestBeacon

}
