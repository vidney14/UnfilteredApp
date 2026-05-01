package com.example.unfilteredapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unfilteredapp.R
import com.example.unfilteredapp.data.model.PlaceResult
import com.example.unfilteredapp.viewmodel.PlacesViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun DetoxScreen(onBack: () -> Unit, viewModel: PlacesViewModel = viewModel()) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val places by viewModel.places.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val singapore = LatLng(1.3521, 103.8198) // Default coordinate
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 14f)
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedPlace by remember { mutableStateOf<PlaceResult?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Get real GPS location and move camera to it
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val loc = LatLng(it.latitude, it.longitude)
                    userLocation = loc
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(loc, 14f)
                }
            }
        }
    }

    // Research-based categories for genuine mental detox
    val filters = listOf(
        FilterItem("Parks", Icons.Default.Park, "park", Color(0xFF4CAF50)),
        FilterItem("Libraries", Icons.Default.MenuBook, "library", Color(0xFF795548)),
        FilterItem("Museums", Icons.Default.Museum, "museum", Color(0xFF673AB7)),
        FilterItem("Spas", Icons.Default.Spa, "spa", Color(0xFFE91E63)),
        FilterItem("Cafes", Icons.Default.Coffee, "cafe", Color(0xFFFF9800)),
        FilterItem("Bookstores", Icons.Default.AutoStories, "book_store", Color(0xFF3F51B5)),
        FilterItem("Yoga", Icons.Default.SelfImprovement, "gym", Color(0xFF00BCD4))
    )

    var selectedFilterItem by remember { mutableStateOf(filters[0]) }

    // Fetch whenever filter changes OR user location first becomes available
    // Only fetch if we actually have a real GPS location — don't silently fall back to Singapore
    LaunchedEffect(selectedFilterItem, userLocation) {
        val loc = userLocation
        if (loc != null) {
            viewModel.fetchNearbyPlaces(loc.latitude, loc.longitude, selectedFilterItem.apiType)
        }
    }

    val mapProperties by remember(locationPermissionGranted) {
        mutableStateOf(MapProperties(
            isMyLocationEnabled = locationPermissionGranted,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        ))
    }

    val uiSettings by remember {
        mutableStateOf(MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false))
    }

    // Show a permission denied banner if user denied location access
    if (!locationPermissionGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings,
                properties = remember { MapProperties(
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                ) }
            )
            // Dimmed overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(32.dp)
                        .shadow(16.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Location Access Needed",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Unfiltered uses your location to find nearby parks, cafes, and calming spaces for your digital detox.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open App Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties
        ) {
            // key() forces MarkerComposable to fully recompose when the selected
            // filter changes — this fixes the stale icon bug
            places.forEach { place ->
                key(selectedFilterItem.apiType, place.place_id) {
                    MarkerComposable(
                        state = MarkerState(position = LatLng(place.geometry.location.lat, place.geometry.location.lng)),
                        onClick = {
                            selectedPlace = place
                            showSheet = true
                            true
                        }
                    ) {
                        CustomMarkerIcon(selectedFilterItem.color, selectedFilterItem.icon)
                    }
                }
            }
        }

        // Top UI Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Digital Detox",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Unplug and explore ${selectedFilterItem.label.lowercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp,
                                    color = selectedFilterItem.color
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        val loc = userLocation ?: singapore
                                        viewModel.fetchNearbyPlaces(loc.latitude, loc.longitude, selectedFilterItem.apiType)
                                    },
                                    modifier = Modifier.background(selectedFilterItem.color.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = selectedFilterItem.color)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Premium Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filters.forEach { item ->
                            val isSelected = selectedFilterItem == item
                            Surface(
                                onClick = { selectedFilterItem = item },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) item.color else item.color.copy(alpha = 0.05f),
                                modifier = Modifier.animateContentSize()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (isSelected) Color.White else item.color
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button — My Location only (search removed)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val loc = LatLng(it.latitude, it.longitude)
                            userLocation = loc
                            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(loc, 15f))
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
    }

    // Detail Bottom Sheet
    if (showSheet && selectedPlace != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            PlaceDetailContent(selectedPlace!!, selectedFilterItem)
        }
    }
}

@Composable
fun CustomMarkerIcon(color: Color, icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(color, RoundedCornerShape(14.dp))
            .border(2.dp, Color.White, RoundedCornerShape(14.dp))
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = Color.White
        )
    }
}

@Composable
fun PlaceDetailContent(place: PlaceResult, category: FilterItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = category.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    category.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(24.dp),
                    tint = category.color
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = category.color
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = place.vicinity ?: "Address not available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        val detailContext = LocalContext.current
        Button(
            onClick = {
                val uri = Uri.parse(
                    "geo:${place.geometry.location.lat},${place.geometry.location.lng}" +
                    "?q=${Uri.encode(place.name)}"
                )
                detailContext.startActivity(Intent(Intent.ACTION_VIEW, uri))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = category.color),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Navigation, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Get Directions", fontWeight = FontWeight.Bold)
        }
    }
}

data class FilterItem(
    val label: String,
    val icon: ImageVector,
    val apiType: String,
    val color: Color
)
