/**
 *  * DMA Laboratory 2
 * @author      : Dimitri De Bleser, Vincent Peer
 * Date         : 16.04.2023
 * Description  : Scan for iBeacons, display the closest one and a list of nearby beacons
 */

package ch.heigvd.iict.dma.labo2

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import ch.heigvd.iict.dma.labo2.databinding.ActivityMainBinding
import org.altbeacon.beacon.*
import org.altbeacon.beacon.BeaconManager.getInstanceForApplication


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val beaconsViewModel : BeaconsViewModel by viewModels()

    private val permissionsGranted = MutableLiveData(false)

    private lateinit var rangingObserver : Observer<Collection<Beacon>>

    private lateinit var beaconManager: BeaconManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // check if bluetooth is enabled
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if(!bluetoothManager.adapter.isEnabled) {
            Toast.makeText(this, R.string.ble_unavailable, Toast.LENGTH_SHORT).show()
            finish()
        }

        // we request permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBeaconsPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN))
        }
        else {
            requestBeaconsPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }


        // init views
        val beaconAdapter = BeaconsAdapter()
        binding.beaconsList.adapter = beaconAdapter
        binding.beaconsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        beaconsViewModel.placeName[BEACON1_MINOR] = "Salon"
        beaconsViewModel.placeName[BEACON2_MINOR] = "Cuisine"

        // update views
        beaconsViewModel.closestBeacon.observe(this) {beacon ->
            if(beacon != null) {
                binding.location.text = "${beacon.major} - ${beacon.minor} (${String.format("%.2f", beacon.distance)}m)\n${beaconsViewModel.placeName[beacon.minor]}"
            } else {
                binding.location.text = getString(R.string.no_beacons)
            }
        }


        beaconsViewModel.nearbyBeacons.observe(this) { nearbyBeacons ->
            if(nearbyBeacons.isNotEmpty()) {
                binding.beaconsList.visibility = View.VISIBLE
                binding.beaconsListEmpty.visibility = View.INVISIBLE
            } else {
                binding.beaconsList.visibility = View.INVISIBLE
                binding.beaconsListEmpty.visibility = View.VISIBLE
            }
            beaconAdapter.items = nearbyBeacons
        }

        // Start ranging
        beaconManager = getInstanceForApplication(this)
        beaconManager.beaconParsers.add(
            BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )

        // Observer for ranging
        rangingObserver = Observer { beacons ->
            Log.d("rangingObserver", "Ranged: ${beacons.count()} beacons")
            for (beacon: Beacon in beacons) {
                Log.d("rangingObserver", "$beacon about ${beacon.distance} meters away")
            }
            beaconsViewModel.updateBeacons(beacons)
        }
        val region = Region("all-beacons-region", null, null, null)
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(this, rangingObserver)
        beaconManager.startRangingBeacons(region)
    }


    private val requestBeaconsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        val isBLEScanGranted =  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false)
        else
            true
        val isFineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val isCoarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        if (isBLEScanGranted && (isFineLocationGranted || isCoarseLocationGranted) ) {
            // Permission is granted. Continue the action
            permissionsGranted.postValue(true)
        }
        else {
            // Explain to the user that the feature is unavailable
            Toast.makeText(this, R.string.ibeacon_feature_unavailable, Toast.LENGTH_SHORT).show()
            permissionsGranted.postValue(false)
        }
    }

    // Becons minor values
    companion object {
        const val BEACON1_MINOR = 55
        const val BEACON2_MINOR = 96
    }

}