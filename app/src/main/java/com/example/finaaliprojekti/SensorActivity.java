package com.example.finaaliprojekti;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 123;
    private EditText locationInput;
    private Button fetchButton;
    private WeatherDataAdapter adapter;
    private List<Pair<String, String>> weatherDataList = new ArrayList<>();
    private String baseUri = "https://opendata.fmi.fi/wfs/fin?service=WFS&version=2.0.0&request=GetFeature&storedquery_id=fmi::observations::weather::timevaluepair&place=";
    private String parameterUri = "&parameters=t2m&";
    private SensorManager mSensorManager;
    private Sensor mSensorLight;
    private Sensor proximitySensor;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor temperatureSensor;

    private TextView mTextSensorLight;
    private TextView sensorStatusTV;
    private TextView mTextOrientation;
    private TextView mTextAccelerometer;
    private TextView mTextGyroscope;
    private TextView mTextTemperature;

    private float[] gravity;
    private float[] geomagnetic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_weather);

        locationInput = findViewById(R.id.location_input);
        fetchButton = findViewById(R.id.fetch_button);
        adapter = new WeatherDataAdapter(weatherDataList);
        RecyclerView rvWeatherData = findViewById(R.id.WeatherData);
        rvWeatherData.setLayoutManager(new LinearLayoutManager(this));
        rvWeatherData.setAdapter(adapter);
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("WeatherData", "button press");
                String location = locationInput.getText().toString();
                String uri = baseUri + location + parameterUri;
                Log.d("WeatherData", uri);
                fetchWeatherData(uri);


            }
        });



        // Initialize sensors
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mTextSensorLight = findViewById(R.id.sensorStatus_light);
        sensorStatusTV = findViewById(R.id.sensorStatus_proximity);
        mTextAccelerometer = findViewById(R.id.sensorStatus_accelerometer);
        mTextGyroscope = findViewById(R.id.sensorStatus_gyroscope);
        mTextTemperature = findViewById(R.id.sensorStatus_temperature);

        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        temperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (mSensorLight == null) {
            mTextSensorLight.setText(R.string.error_no_sensor);
        }

        if (proximitySensor == null) {
            sensorStatusTV.setText(R.string.error_no_sensor);
        }

        if (accelerometer == null) {
            mTextAccelerometer.setText(R.string.error_no_sensor);
        }

        if (gyroscope == null) {
            mTextGyroscope.setText(R.string.error_no_sensor);
        }

        if (temperatureSensor == null) {
            mTextTemperature.setText(R.string.error_no_sensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] values = event.values;

        switch (sensorType) {
            case Sensor.TYPE_LIGHT:
                mTextSensorLight.setText(getResources().getString(R.string.label_light, values[0]));
                break;
            case Sensor.TYPE_PROXIMITY:
                sensorStatusTV.setText(getString(R.string.label_proximity, values[0]));
                break;
            case Sensor.TYPE_ACCELEROMETER:
                gravity = values;
                updateOrientation();
                mTextAccelerometer.setText(getString(R.string.label_accelerometer, values[0], values[1], values[2]));
                break;
            case Sensor.TYPE_GYROSCOPE:
                mTextGyroscope.setText(getString(R.string.label_gyroscope, values[0], values[1], values[2]));
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                mTextTemperature.setText(getString(R.string.label_temperature, values[0]));
                break;
            default:
                // Do nothing
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
    }

    private void updateOrientation() {
        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                int azimuth = (int) Math.toDegrees(orientation[0]);
                int pitch = (int) Math.toDegrees(orientation[1]);
                int roll = (int) Math.toDegrees(orientation[2]);

                String orientationText = "Azimuth: " + azimuth + "\nPitch: " + pitch + "\nRoll: " + roll;
                mTextOrientation.setText(orientationText);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorLight != null) {
            mSensorManager.registerListener(this, mSensorLight, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (proximitySensor != null) {
            mSensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gyroscope != null) {
            mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (temperatureSensor != null) {
            mSensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    public void fetchWeatherData(String uri) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // String url = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseXML(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("fetchWeatherData", "e toimi");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }
    public void parseXML(String response) {


        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            Log.d("WeatherData", "parse");
            xpp.setInput(new StringReader(response));
            int eventType = xpp.getEventType();

            String time = null;
            String temperature = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "MeasurementTVP".equals(xpp.getName())) {
                    while (!(eventType == XmlPullParser.END_TAG && "MeasurementTVP".equals(xpp.getName()))) {
                        if (eventType == XmlPullParser.START_TAG) {
                            Log.d("WeatherData", xpp.getName());
                            if ("time".equals(xpp.getName())) {
                                xpp.next();
                                time = xpp.getText();
                            } else if ("value".equals(xpp.getName())) {
                                xpp.next();
                                temperature = xpp.getText() + "°C";
                                Log.d("WeatherData", "Time: " + time + ", Temperature: " + temperature);
                            }
                        }
                        eventType = xpp.next();
                    }
                    if (time != null && temperature != null) {
                        String[] parts = time.split("T");
                        String formattedDateTime = parts[0] + " " + parts[1].replace("Z", "");
                        weatherDataList.add(new Pair<>(formattedDateTime, temperature));

                    }
                }
                eventType = xpp.next();
            }
            Collections.reverse(weatherDataList);
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        } catch (XmlPullParserException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
