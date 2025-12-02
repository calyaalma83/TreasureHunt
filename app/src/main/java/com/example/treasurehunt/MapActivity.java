package com.example.treasurehunt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.annotations.Icon;
import org.maplibre.android.annotations.IconFactory;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final float CHECKPOINT_RADIUS = 20f; // radius dalam meter

    // Views
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private TextView tvMissionTitle, tvCheckpointProgress, tvDistance, tvClue, tvCompletedCheckpoint;
    private LinearLayout clueCard;
    private RelativeLayout missionCompleteCard;
    private Button btnCenterLocation, btnShowClue, btnCloseClue, btnNextCheckpoint;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;

    // Game Data
    private ArrayList<Checkpoint> checkpoints;
    private int currentCheckpointIndex = 0;
    private int completedCount = 0;
    private boolean checkpointsGenerated = false; // Flag untuk cek sudah generate atau belum

    // Map Style URL (gunakan style yang reliable)
    private static final String STYLE_URL = "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json";

    private Icon iconRed;
    private Icon iconGrey;

    private Bitmap getBitmapFromVector(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableId);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize MapLibre
        MapLibre.getInstance(this);

        setContentView(R.layout.activity_map);

        initViews();
        initCheckpoints();
        initLocationClient();

        // Cek dan minta permission lokasi dulu
        if (checkLocationPermission()) {
            initMapView(savedInstanceState);
        } else {
            requestLocationPermission();
            initMapView(savedInstanceState);
        }

        setupClickListeners();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        tvMissionTitle = findViewById(R.id.tvMissionTitle);
        tvCheckpointProgress = findViewById(R.id.tvCheckpointProgress);
        tvDistance = findViewById(R.id.tvDistance);
        tvClue = findViewById(R.id.tvClue);
        tvCompletedCheckpoint = findViewById(R.id.tvCompletedCheckpoint);
        clueCard = findViewById(R.id.clueCard);
        missionCompleteCard = findViewById(R.id.missionCompleteCard);
        btnCenterLocation = findViewById(R.id.btnCenterLocation);
        btnShowClue = findViewById(R.id.btnShowClue);
        btnCloseClue = findViewById(R.id.btnCloseClue);
        btnNextCheckpoint = findViewById(R.id.btnNextCheckpoint);
        IconFactory iconFactory = IconFactory.getInstance(this);
        Bitmap redBitmap = getBitmapFromVector(R.drawable.ic_marker_red);
        Bitmap greyBitmap = getBitmapFromVector(R.drawable.ic_marker_grey);
        iconRed = iconFactory.fromBitmap(redBitmap);
        iconGrey = iconFactory.fromBitmap(greyBitmap);

    }

    private void initCheckpoints() {
        // Checkpoint akan di-generate setelah dapat lokasi user
        // Jadi kosongkan dulu
        checkpoints = new ArrayList<>();
        updateUI();
    }

    /**
     * Generate checkpoint di sekitar lokasi user saat ini
     */
    private void generateCheckpointsAroundUser() {
        if (currentLocation != null && !checkpointsGenerated) {
            checkpoints = CheckpointData.generateCheckpointsAroundUser(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            checkpointsGenerated = true;

            // Tambahkan marker ke peta
            addCheckpointMarkers();
            updateUI();

            // Arahkan kamera ke checkpoint pertama
            if (!checkpoints.isEmpty()) {
                Checkpoint first = checkpoints.get(0);
                if (mapLibreMap != null) {
                    mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(first.getLatitude(), first.getLongitude()), 17));
                }
            }
        }
    }

    private void initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;

                    // Generate checkpoint otomatis saat pertama kali dapat lokasi
                    if (!checkpointsGenerated) {
                        generateCheckpointsAroundUser();
                    }

                    updateDistanceToCheckpoint();
                    checkProximityToCheckpoint();
                }
            }
        };
    }

    private void initMapView(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            mapLibreMap = map;

            map.setStyle(new Style.Builder().fromUri(STYLE_URL), style -> {
                enableLocationComponent(style);

                // Marker akan ditambahkan setelah checkpoint di-generate
                // addCheckpointMarkers() dipanggil di generateCheckpointsAroundUser()
            });

            // Marker click listener untuk menampilkan clue
            map.setOnMarkerClickListener(marker -> {
                for (Checkpoint cp : checkpoints) {
                    if (marker.getTitle().equals(cp.getName())) {
                        showClue(cp.getClue());
                        break;
                    }
                }
                return true;
            });
        });
    }

    private void enableLocationComponent(Style style) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationComponent locationComponent = mapLibreMap.getLocationComponent();

            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, style).build()
            );

            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);

            startLocationUpdates();
        } else {
            requestLocationPermission();
        }
    }

    private void addCheckpointMarkers() {
        if (mapLibreMap == null || checkpoints == null) return;

        for (Checkpoint cp : checkpoints) {
            Marker marker = mapLibreMap.addMarker(new MarkerOptions()
                    .position(new LatLng(cp.getLatitude(), cp.getLongitude()))
                    .title(cp.getName())
                    .icon(iconRed)
            );

            cp.setMarker(marker);
        }
    }

    private void setupClickListeners() {
        btnCenterLocation.setOnClickListener(v -> centerOnUserLocation());

        btnShowClue.setOnClickListener(v -> {
            if (currentCheckpointIndex < checkpoints.size()) {
                Checkpoint current = checkpoints.get(currentCheckpointIndex);
                showClue(current.getClue());
            }
        });

        btnCloseClue.setOnClickListener(v -> clueCard.setVisibility(View.GONE));

        btnNextCheckpoint.setOnClickListener(v -> {
            missionCompleteCard.setVisibility(View.GONE);
            moveToNextCheckpoint();
        });
    }

    private void showClue(String clue) {
        tvClue.setText(clue);
        clueCard.setVisibility(View.VISIBLE);
    }

    private void centerOnUserLocation() {
        if (currentLocation != null && mapLibreMap != null) {
            LatLng userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));
        } else {
            // Coba ambil lokasi terakhir
            if (checkLocationPermission()) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                currentLocation = location;
                                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                if (mapLibreMap != null) {
                                    mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));
                                }
                                // Generate checkpoint jika belum
                                if (!checkpointsGenerated) {
                                    generateCheckpointsAroundUser();
                                }
                            } else {
                                Toast.makeText(this, "Aktifkan GPS dan tunggu sebentar...", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show();
                requestLocationPermission();
            }
        }
    }

    private void updateDistanceToCheckpoint() {
        if (currentLocation == null || currentCheckpointIndex >= checkpoints.size()) return;

        Checkpoint currentCp = checkpoints.get(currentCheckpointIndex);
        float distance = calculateDistance(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                currentCp.getLatitude(),
                currentCp.getLongitude()
        );

        tvDistance.setText(String.format("%.0f m", distance));
    }

    private void checkProximityToCheckpoint() {
        if (currentLocation == null || currentCheckpointIndex >= checkpoints.size()) return;

        Checkpoint currentCp = checkpoints.get(currentCheckpointIndex);
        float distance = calculateDistance(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                currentCp.getLatitude(),
                currentCp.getLongitude()
        );

        if (distance <= CHECKPOINT_RADIUS && !currentCp.isCompleted()) {
            completeCurrentCheckpoint();
        }
    }

    private void completeCurrentCheckpoint() {
        Checkpoint currentCp = checkpoints.get(currentCheckpointIndex);
        currentCp.setCompleted(true);
        completedCount++;

        // Ganti marker jadi abu-abu
        if (currentCp.getMarker() != null) {
            Marker oldMarker = currentCp.getMarker();
            mapLibreMap.removeMarker(oldMarker);

            Marker greyMarker = mapLibreMap.addMarker(new MarkerOptions()
                    .position(new LatLng(currentCp.getLatitude(), currentCp.getLongitude()))
                    .title(currentCp.getName())
                    .icon(iconGrey)
            );

            currentCp.setMarker(greyMarker);
        }

        // Popup success
        tvCompletedCheckpoint.setText("[ " + currentCp.getName().toUpperCase() + " BERHASIL DITEMUKAN ]");
        missionCompleteCard.setVisibility(View.VISIBLE);

        if (completedCount >= checkpoints.size()) {
            btnNextCheckpoint.setText(">> FINISH GAME");
            btnNextCheckpoint.setOnClickListener(v -> {
                Intent intent = new Intent(MapActivity.this, FinishActivity.class);
                intent.putExtra("totalCheckpoints", checkpoints.size());
                intent.putExtra("totalScore", completedCount * 100);
                startActivity(intent);
                finish();
            });
        }
    }

    private void moveToNextCheckpoint() {
        currentCheckpointIndex++;

        if (currentCheckpointIndex < checkpoints.size()) {
            updateUI();

            // Arahkan kamera ke checkpoint berikutnya
            Checkpoint nextCp = checkpoints.get(currentCheckpointIndex);
            if (mapLibreMap != null) {
                mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(nextCp.getLatitude(), nextCp.getLongitude()), 16));
            }
        }
    }

    private void updateUI() {
        if (currentCheckpointIndex < checkpoints.size()) {
            Checkpoint current = checkpoints.get(currentCheckpointIndex);
            tvMissionTitle.setText(current.getName());
            tvCheckpointProgress.setText(completedCount + "/" + checkpoints.size());
        }
    }

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    // ==================== LOCATION PERMISSION ====================

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location
                if (mapLibreMap != null && mapLibreMap.getStyle() != null) {
                    enableLocationComponent(mapLibreMap.getStyle());
                }
                startLocationUpdates();

                // Coba ambil lokasi terakhir untuk generate checkpoint
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                currentLocation = location;
                                if (!checkpointsGenerated) {
                                    generateCheckpointsAroundUser();
                                }
                            }
                        });
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan untuk bermain game", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(2000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(1000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // ==================== LIFECYCLE ====================

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}