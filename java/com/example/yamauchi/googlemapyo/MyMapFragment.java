package com.example.yamauchi.googlemapyo;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yamauchi on 2015/08/19.
 */
public class MyMapFragment extends MapFragment {

    protected GoogleMap googleMap;                     // マップオブジェクト
    protected HashMap<Marker, EkiInfo> ekiMarkerMap;  // マーカーと駅情報のHashMap
    private CameraPosition centerPosition = null;    // 地図の中心位置
    private Boolean noCameraChange = false;         // 地図移動の抑制フラグ

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ekiMarkerMap = new HashMap<Marker, EkiInfo>();
        setRetainInstance(true); // フラグメントを保存する
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            // マップオブジェクトを取得する
            googleMap = getMap();

            // 初期設定がまだなら（1）
            if (centerPosition == null) {
                mapInit();
            }

            // 地図の中心位置を取得する
            centerPosition = googleMap.getCameraPosition();

            // 最寄り駅情報を取得する
            execMoyori(centerPosition);

            // 地図を移動したときのリスナー
            googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    // 前回から500メートルより大きく移動したら
                    if (0.5 < calcDistance(centerPosition, cameraPosition)) {
                        execMoyori(cameraPosition);
                        centerPosition = cameraPosition;
                    }
                }
            });

            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    // 駅情報の取り出し
                    EkiInfo e = ekiMarkerMap.get(marker);
                    Toast ts = Toast.makeText(getActivity().getBaseContext(), e.name + "("
                            + e.distance + "m)\n" + "前の駅:" + e.prev + "\n次の駅:"
                            + e.next + "\n" + e.line, Toast.LENGTH_LONG);
                    ts.setGravity(Gravity.TOP, 0, 200);
                    ts.show();
                }
            });
        }
        // GoogleMapが使用できないとき
        catch (Exception e) {
        }
    }

    // 2点間の距離を求める(km)（2）
    private double calcDistance(CameraPosition a, CameraPosition b) {

        double lata = Math.toRadians(a.target.latitude);
        double lnga = Math.toRadians(a.target.longitude);

        double latb = Math.toRadians(b.target.latitude);
        double lngb = Math.toRadians(b.target.longitude);

        double r = 6378.137; // 赤道半径

        return r * Math.acos(Math.sin(lata) * Math.sin(latb)
                + Math.cos(lata) * Math.cos(latb) * Math.cos(lngb - lnga));
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

        execMoyori(camerapos);
    }

    // 地図の中心位置を取得して、APIのURLを準備する
    public void execMoyori(CameraPosition centerPosition) {

        Bundle bundle = new Bundle();

        // 緯度
        bundle.putString("y", Double.toString(centerPosition.target.latitude));
        // 経度
        bundle.putString("x", Double.toString(centerPosition.target.longitude));

        bundle.putString("moyori",
                "http://express.heartrails.com/api/json?method=getStations&");

        // LoaderManagerの初期化
        getLoaderManager().restartLoader(0, bundle, (MainActivity) getActivity());
    }

    // マーカーを設定する
    public void setMarker(ParseMoyori parseMoyori) {

        GoogleMap googleMap = getMap();

        // マーカーをいったん削除しておく
        googleMap.clear();
        ekiMarkerMap.clear();

        // APIの結果をマーカーに反映する（2）
        for (EkiInfo e : parseMoyori.getEkiinfo()) {

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(e.y, e.x))
                    .title(e.name)
                    .snippet(e.line)
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_train))); // (3)

            // マーカーと駅情報を保管しておく（4）
            ekiMarkerMap.put(marker, e);
        }
    }

    // 駅情報からマーカーを求める
    public Marker getKeyByValue(EkiInfo value) {
        for (Map.Entry<Marker, EkiInfo> entry : ekiMarkerMap.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    // 駅情報に合致するマーカーの情報ウィンドウを表示し、地図の中心にする
    public void moveEkiToCenter(EkiInfo value) {

        // 駅情報からマーカーを取得
        Marker marker = getKeyByValue(value);
        // 情報ウィンドウを表示
        marker.showInfoWindow();

        // マーカーの位置に、地図の中心を移動する
        CameraPosition cameraposprev = googleMap.getCameraPosition();
        CameraPosition camerapos =
                new CameraPosition.Builder(cameraposprev).target(marker.getPosition()).build();
        noCameraChange = true;
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camerapos));
    }

}
