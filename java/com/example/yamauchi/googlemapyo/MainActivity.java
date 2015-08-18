package com.example.yamauchi.googlemapyo;

import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String>
{

    // マップオブジェクト（1）
    private GoogleMap googleMap;
    // マーカーと駅情報のHashMap（1）
    private HashMap<Marker, EkiInfo> ekiMarkerMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ekiMarkerMap = new HashMap<Marker, EkiInfo>();

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

        execMoyori();
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

    // 地図の中心位置を取得して、APIのURLを準備する
    public void execMoyori() {

        // 地図の中心位置の取得
        CameraPosition cameraPos = googleMap.getCameraPosition();

        Bundle bundle = new Bundle();
        // 緯度
        bundle.putString("y", Double.toString(cameraPos.target.latitude));
        // 経度
        bundle.putString("x", Double.toString(cameraPos.target.longitude));

        bundle.putString("moyori",
                "http://express.heartrails.com/api/json?method=getStations&");

        // LoaderManagerの初期化（1）
        getLoaderManager().restartLoader(0, bundle, this);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle bundle) {
        HttpAsyncLoader2 loader = null;
        switch (id) {
            case 0:
                // リクエストURLの組み立て
                String url = bundle.getString("moyori")
                        + "x=" + bundle.getString("x") + "&"
                        + "y=" + bundle.getString("y");

                loader = new HttpAsyncLoader2(this, url);

                // Web APIにアクセスする（2）
                loader.forceLoad();
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String body) {
        // APIの取得に失敗の場合
        if (body == null) return;

        switch (loader.getId()) {

            case 0:

                // APIの結果を解析する
                ParseMoyori parse = new ParseMoyori();
                parse.loadJson(body);

                // マーカーをいったん削除しておく
                googleMap.clear();
                ekiMarkerMap.clear();

                // APIの結果をマーカーに反映する（2）
                for (EkiInfo e : parse.getEkiinfo()) {

                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(e.y, e.x))
                            .title(e.name)
                            .snippet(e.line)
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.ic_train))); // （3）

                    // マーカーと駅情報を保管しておく（4）
                    ekiMarkerMap.put(marker, e);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
