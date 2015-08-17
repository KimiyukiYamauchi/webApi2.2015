package com.example.yamauchi.googlemapyo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {

    // マップオブジェクト（1）
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // MapFragmentの取得（2）
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        try {
            // マップオブジェクトを取得する（3）
            googleMap = mapFragment.getMap();

            // Activityが初めて生成されたとき（4）
            if (savedInstanceState == null) {

                // フラグメントを保存する（5）
                mapFragment.setRetainInstance(true);

                // 地図の初期設定を行う（6）
                mapInit();
            }
        }
        // GoogleMapが使用できないとき
        catch (Exception e) {
        }
    }

    // 地図の初期設定
    private void mapInit() {

        // 地図タイプ設定（1）
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // 現在位置ボタンの表示（2）
        googleMap.setMyLocationEnabled(true);

        // 東京駅の位置、ズーム設定（3）
        CameraPosition camerapos = new CameraPosition.Builder()
                .target(new LatLng(35.681382, 139.766084)).zoom(15.5f).build();

        // 地図の中心を変更する（4）
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camerapos));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
