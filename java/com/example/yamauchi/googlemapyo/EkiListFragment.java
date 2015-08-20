package com.example.yamauchi.googlemapyo;

import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * Created by yamauchi on 2015/08/19.
 * 最寄り駅情報を一覧表示するFragment
 */
public class EkiListFragment extends ListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Fragmentを保存する
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView list = getListView();
        // 背景を白にする（8）
        list.setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            // クリックした行の駅情報を取得
            EkiInfo e = (EkiInfo)getListAdapter().getItem(position);

            MyMapFragment mapFragment = (MyMapFragment)getFragmentManager().findFragmentById(R.id.map);
            // 駅情報に合致するマーカーの情報ウィンドウを表示し、地図の中心にする
            mapFragment.moveEkiToCenter(e);

        }
        catch (Exception e) {
        }
    }

}
