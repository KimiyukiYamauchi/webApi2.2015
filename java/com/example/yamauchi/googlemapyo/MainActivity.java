package com.example.yamauchi.googlemapyo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String>
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    // ListFragmentの表示・非表示、ナビゲーションアイコンの表示を切り替える（1）
    public void toggleList(boolean hide) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction(); //（2）
        ListFragment listFragment =
                (ListFragment)fm.findFragmentById(R.id.eki_list); //（3）

        // 引数のhideがtrueまたはListFragmentが表示されているとき
        if (hide==true || listFragment.isVisible()) {
            // ListFragmentを非表示にする
            transaction.hide(listFragment);
            // ナビゲーションアイコンを表示する（4）
            //getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        else {
            // ListFragmentを表示する
            transaction.show(listFragment);
            // ナビゲーションアイコンを非表示にする
            //getActionBar().setDisplayHomeAsUpEnabled(false);
        }
        // Fragmentへの操作を確定する
        transaction.commit(); //（5）
    }


//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        // 1ペインなら
//        if (getResources().getBoolean(R.bool.is_one_pane)) {
//            // アクションバーのアイコンがクリックされたとき（6）
//            if (android.R.id.home == item.getItemId()) {
//                toggleList(false);
//            }
//        }
//        return super.onMenuItemSelected(featureId, item);
//    }

    @Override
    protected void onResume() { //（7）
        super.onResume();
        // 1ペインならListFragmentを非表示、2ペインなら表示する
        toggleList(getResources().getBoolean(R.bool.is_one_pane));
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

                // Web APIにアクセスする
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

                FragmentManager fm = getFragmentManager();
                MyMapFragment mapFragment = (MyMapFragment) fm.findFragmentById(R.id.map);

                // マーカーを設定する
                mapFragment.setMarker(parse);

                // 駅情報のアダプターを作成する
                ArrayAdapter<EkiInfo> adapter = new ArrayAdapter<EkiInfo>(this,
                        android.R.layout.simple_list_item_1,
                        parse.getEkiinfo());
                try {
                    ListFragment listFragment = (ListFragment) fm.findFragmentById(R.id.eki_list);

                    // ListFragmentに駅情報のアダプターを設定する
                    listFragment.setListAdapter(adapter);

                    break;
                }
                catch (Exception e) {
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
