/**
 *  * DMA Laboratory 2
 * @author      : Dimitri De Bleser, Vincent Peer
 * Date         : 16.04.2023
 * Description  : Manages the beacons list and the closest beacon liveData
 */

package ch.heigvd.iict.dma.labo2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import ch.heigvd.iict.dma.labo2.models.PersistentBeacon
import org.altbeacon.beacon.Beacon
import java.util.*


class BeaconsViewModel : ViewModel() {
    fun updateBeacons(beacons: Collection<Beacon>) {
        val newBeacons = beacons.map { b -> PersistentBeacon(major = b.id2.toInt(), minor = b.id3.toInt(), uuid = UUID.randomUUID(), rssi = b.rssi, txPower = b.txPower, distance = b.distance) }
        _nearbyBeacons.value = newBeacons.toMutableList()
        _closestBeacon.value = newBeacons.minByOrNull { b -> b.distance }
    }

    private val _nearbyBeacons = MutableLiveData(mutableListOf<PersistentBeacon>())
    val nearbyBeacons : LiveData<List<PersistentBeacon>> = _nearbyBeacons.map { l -> l.toList().map { el -> el.copy() } }

    private val _closestBeacon = MutableLiveData<PersistentBeacon?>(null)
    val closestBeacon : LiveData<PersistentBeacon?> get() = _closestBeacon

    // public attribute to test the position functionality
    val placeName = mutableMapOf<Int, String>()

}
