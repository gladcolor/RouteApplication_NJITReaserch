package com.example.bhati.routeapplication;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

	private List<Marker> markers = new ArrayList<Marker>();
	private MapboxMap googleMap;
	private final Handler mHandler = new Handler(); 
	private Marker selectedMarker; 
	private Animator animator = new Animator(); 

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button start = (Button)findViewById(R.id.btn_play);
		Button stop = (Button)findViewById(R.id.btn_stop);
		Button ResetMarker = (Button)findViewById(R.id.btn_clearmarker);

		ResetMarker.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View v) {
				clearMarkers();
			}
		});

		start.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View v) {
				animator.startAnimation(false);
			}
		});

		stop.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View v) {
				animator.stopAnimation();
			}
		});

		loadMap();
		googleMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {

			@Override
			public void onMapClick(LatLng latLng) {
				addMarkerToMap(latLng);
				animator.startAnimation(false);
			} 
		});   
	}

	public void clearMarkers() {
		googleMap.clear();
		markers.clear();		
	}

	protected void removeSelectedMarker() {
		this.markers.remove(this.selectedMarker);
		this.selectedMarker.remove();
	}

	protected void addMarkerToMap(LatLng latLng) {
		Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title("title").snippet("snippet"));
		markers.add(marker);
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		googleMap = mapboxMap;
	}

	public class Animator implements Runnable { 
		private static final int ANIMATE_SPEEED = 1500;
		private static final int ANIMATE_SPEEED_TURN = 1000;
		private static final int BEARING_OFFSET = 20; 
		private final Interpolator interpolator = new LinearInterpolator(); 
		int currentIndex = 0; 
		float tilt = 90;
		float zoom = 15.5f;
		boolean upward=true; 
		long start = SystemClock.uptimeMillis(); 
		LatLng endLatLng = null; 
		LatLng beginLatLng = null; 
		boolean showPolyline = false; 
		private Marker trackingMarker; 

		public void reset() {
			resetMarkers();
			start = SystemClock.uptimeMillis();
			currentIndex = 0;
			endLatLng = getEndLatLng();
			beginLatLng = getBeginLatLng();

		}

		private void resetMarkers() {
			for (Marker marker : markers) {
				IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
				//  Drawable iconDrawable = ContextCompat.getDrawable(SavingActivity.this, R.drawable.marker_red);
				Icon icon = null;
				icon = iconFactory.fromResource(R.drawable.marker_moveable);
				marker.setIcon(icon);

				//marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			}
		}

		public void stop() {
			trackingMarker.remove();
			mHandler.removeCallbacks(animator); 
		}

		private void highLightMarker(int index) {
			highLightMarker(markers.get(index));
		}

		private void highLightMarker(Marker marker) { 
			//marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

			IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
			//  Drawable iconDrawable = ContextCompat.getDrawable(SavingActivity.this, R.drawable.marker_red);
			Icon icon = null;
			icon = iconFactory.fromResource(R.drawable.marker_red);
			marker.setIcon(icon);
			//marker.showInfoWindow(googleMap);
			selectedMarker=marker;

		}

		public void initialize(boolean showPolyLine) {
			reset();
			this.showPolyline = showPolyLine; 
			highLightMarker(0); 
			if (showPolyLine) {
				polyLine = initializePolyLine();
			} 
			// We first need to put the camera in the correct position for the first run (we need 2 markers for this).....
			LatLng markerPos = markers.get(0).getPosition();
			LatLng secondPos = markers.get(1).getPosition(); 
			setupCameraPositionForMovement(markerPos, secondPos); 
		}

		private void setupCameraPositionForMovement(LatLng markerPos, LatLng secondPos) {
			float bearing = bearingBetweenLatLngs(markerPos,secondPos);
			trackingMarker = googleMap.addMarker(new MarkerOptions().position(markerPos)
					.title("title")
					.snippet("snippet")); 
			CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(markerPos)
			.bearing(bearing + BEARING_OFFSET)
			.tilt(60)
			.zoom(googleMap.getCameraPosition().zoom >=16 ? googleMap.getCameraPosition().zoom : 16)
			.build();

			googleMap.animateCamera(
					CameraUpdateFactory.newCameraPosition(cameraPosition), 
					ANIMATE_SPEEED_TURN,
					new MapboxMap.CancelableCallback() {
						@Override
						public void onFinish() {
							System.out.println("finished camera");
							Log.e("animator before reset", animator +"");
							animator.reset();
							Log.e("animator after reset", animator +"");
							Handler handler = new Handler();
							handler.post(animator);	
						} 
						@Override
						public void onCancel() {
							System.out.println("cancelling camera");									
						}
					});
		}		

		public void navigateToPoint(LatLng latLng, boolean animate) {
			CameraPosition position = new CameraPosition.Builder().target(latLng).build();
			changeCameraPosition(position, animate);
		}

		private void changeCameraPosition(CameraPosition cameraPosition, boolean animate) {
			CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
			if (animate) {
				googleMap.animateCamera(cameraUpdate);
			} else {
				googleMap.moveCamera(cameraUpdate);
			} 
		}

		private Location convertLatLngToLocation(LatLng latLng) {
			Location loc = new Location("someLoc");
			loc.setLatitude(latLng.getLatitude());
			loc.setLongitude(latLng.getLongitude());
			return loc;
		}

		private float bearingBetweenLatLngs(LatLng begin,LatLng end) {
			Location beginL= convertLatLngToLocation(begin);
			Location endL= convertLatLngToLocation(end); 
			return beginL.bearingTo(endL);
		} 


		private Polyline polyLine;
		private PolylineOptions rectOptions = new PolylineOptions();
		private Polyline initializePolyLine() {
			rectOptions.add(markers.get(0).getPosition());
			return googleMap.addPolyline(rectOptions);
		}

		/**
		 * Add the marker to the polyline.
		 */
		private void updatePolyLine(LatLng latLng) {
			List<LatLng> points = polyLine.getPoints();
			points.add(latLng);
			polyLine.setPoints(points);
		} 
		public void stopAnimation() {
			animator.stop();
		} 

		public void startAnimation(boolean showPolyLine) {
			if (markers.size()>2) {
				animator.initialize(showPolyLine);
			}
		}		 
		@Override
		public void run() { 
			long elapsed = SystemClock.uptimeMillis() - start;
			double t = interpolator.getInterpolation((float)elapsed/ANIMATE_SPEEED);
			Log.w("interpolator", t +""); 
			double lat = t * endLatLng.getLatitude() + (1-t) * beginLatLng.getLatitude();
			double lng = t * endLatLng.getLongitude() + (1-t) * beginLatLng.getLongitude();
			Log.w("lat. lng", lat + "," + lng +""); 
			LatLng newPosition = new LatLng(lat, lng);
			Log.w("newPosition", newPosition +"");

			trackingMarker.setPosition(newPosition); 
			if (showPolyline) {
				updatePolyLine(newPosition);
			}

			// It's not possible to move the marker + center it through a cameraposition update while another camerapostioning was already happening.
			//navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
			//navigateToPoint(newPosition,false);

			if (t< 1) {
				mHandler.postDelayed(this, 16);
			} else { 
				System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + markers.size());
				// imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
				if (currentIndex<markers.size()-2) { 
					currentIndex++;  
					endLatLng = getEndLatLng();
					beginLatLng = getBeginLatLng();  
					start = SystemClock.uptimeMillis(); 
					LatLng begin = getBeginLatLng();
					LatLng end = getEndLatLng(); 
					float bearingL = bearingBetweenLatLngs(begin, end); 
					highLightMarker(currentIndex); 
					CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(end) // changed this...
					.bearing(bearingL  + BEARING_OFFSET)
					.tilt(tilt)
					.zoom(googleMap.getCameraPosition().zoom)
					.build(); 
					googleMap.animateCamera(
							CameraUpdateFactory.newCameraPosition(cameraPosition), 
							ANIMATE_SPEEED_TURN,
							null
							); 
					start = SystemClock.uptimeMillis();
					mHandler.postDelayed(animator, 16);		 
				} else {
					currentIndex++;
					highLightMarker(currentIndex);
					stopAnimation();
				} 
			}
		} 
		private LatLng getEndLatLng() {
			return markers.get(currentIndex+1).getPosition();
		} 
		private LatLng getBeginLatLng() {
			return markers.get(currentIndex).getPosition();
		} 
		private void adjustCameraPosition() { 
			if (upward) { 
				if (tilt<90) {
					tilt ++;
					zoom-=0.01f;
				} else {
					upward=false;
				} 
			} else {
				if (tilt>0) {
					tilt --;
					zoom+=0.01f;
				} else {
					upward=true;
				}
			}			
		}
	};	

	private void loadMap() {
		try {
			SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapss);
			supportMapFragment.getMapAsync(this);
		} catch (Exception e) {
			e.toString();
		} 
	}
}
