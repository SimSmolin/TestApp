package ru.taximaster.testapp;

import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mylayouts.GridFragment;
import progressimage.ProgressPicture;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int PAGE_COUNT = 5;            // MAX pages in GridFragment view
    public static final int PPP = 9;                    // ? Photo per page
    public int page = 1;                                // init page in GridFragment view
    public static final String TAG = "testapp";         // log Tag
    public Button button;                               // button "Искать"
    public EditText editText;                           // text for Search
    public static ProgressDialog pd;                    // ?
    public static BitmapDrawable[] bitmaps;             // Picture array
    public ViewPager viewPager;                         // view for pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
    public static PagerAdapter pagerAdapter;            // PagerAdapter for viewPager
    public static HashMap<Integer,Handler> handlerMap = new HashMap<Integer,Handler>();
    ProgressPicture progressPicture;
    public static ArrayList<String> urlList = new ArrayList<String>();
    /*
     * ключ для доступа к api выдается после регистрации в качестве разработчика на сервисе
     * https://www.flickr.com/services/apps/create/noncommercial/?
     */
    private final String KEY = "b0720d83ebcdfaad9e12a8a14d0724e6"; //api key
    private Handler updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
// start added sim
        progressPicture = (ProgressPicture)findViewById(R.id.ProgressPicture);
        progressPicture.stopAnimation();
// stop added sim
        updater = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                pagerAdapter.notifyDataSetChanged();
            }
        };
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                onButtonClick();
                break;
            default:
                break;
        }
    }

    public void onButtonClick() {
        // button.setClickable(false);
        button.setEnabled(false); // added sim
        progressPicture.startAnimation(); // added sim

        page = viewPager.getCurrentItem() + 1;
//        MainActivity.bitmaps = new BitmapDrawable[PPP * page]; // changed sim
        MainActivity.bitmaps = new BitmapDrawable[PPP * PAGE_COUNT];
        // pd.show(); // commented sim

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ImageLoaderAsyncTask imageLoaderAsyncTask = new ImageLoaderAsyncTask();
                    imageLoaderAsyncTask.execute(editText.getText().toString());
                    try {
                        bitmaps = imageLoaderAsyncTask.get();
                        //updater.sendEmptyMessage(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // pd.cancel(); // commented sim

                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public class ImageLoaderAsyncTask extends AsyncTask<String, Integer, BitmapDrawable[]> {
        // ArrayList<String> urlList = new ArrayList<String>(); // transfer in Global
        private JSONObject respJSON;
        BitmapDrawable img = null;

        @Override
        protected BitmapDrawable[] doInBackground(String... params) {
            String[] questRaw = params[0].split(" ");
            String quest = questRaw[0];
            for (int i = 1; i < questRaw.length; i++) {
                quest = quest + "+" + questRaw[i];
            }

            String reqURL = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key="
                    + KEY + "&sort=relevance&content_type=1&per_page="
//                    + (PPP * page) + "&page=1&media=photos&format=json&text='"
                    + (PPP * PAGE_COUNT) + "&page=1&media=photos&format=json&text='"
                    + quest + "'";

            try {
                respJSON = request2server(reqURL);
                urlList = getUrlList(respJSON);

                for (int j = 0; j < urlList.size(); j++) {
                    img = getPhoto(urlList.get(j));
                    bitmaps[j] = img;
                    Log.d(TAG, "загружено " + (j + 1));
                    updater.sendEmptyMessage(0);                // added sim
                    publishProgress(j);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmaps;
        }

        @Override
        protected void onPostExecute(BitmapDrawable bitmap[]) {
            // button.setClickable(true); // commented sim
            button.setEnabled(true); // added sim
            progressPicture.stopAnimation(); // added sim
            pagerAdapter.notifyDataSetChanged(); // обновление представдения
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        public JSONObject request2server(String reqURL) throws Exception {
            String jsonStr = null;
            JSONObject respJSON = null;

            try {
                URL url = new URL(reqURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line = reader.readLine();
                con.disconnect();
                reader.close();
                // line обернуто в jsonFlickrApi(json)
                Pattern p = Pattern.compile(".*?\\((.*)\\)$");
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    jsonStr = m.group(1);
                }

                respJSON = new JSONObject(jsonStr);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return respJSON;
        }

        public ArrayList<String> getUrlList(JSONObject json) {
            ArrayList<String> list = new ArrayList<String>();
            JSONArray photo = null;

            try {
                photo = json.getJSONObject("photos").getJSONArray("photo");
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int i = 0; i < photo.length(); i++) {
                try {
                    JSONObject p = photo.getJSONObject(i);
                    String farm = p.getString("farm");
                    String server = p.getString("server");
                    String id = p.getString("id");
                    String secret = p.getString("secret");
                    String url = "http://farm" + farm + ".static.flickr.com/"
                            + server + "/" + id + "_" + secret + ".jpg";
                    list.add(url);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return list;
        }

        // загружает фото из списка urlList.get(j)
        private BitmapDrawable getPhoto(String photoUrl) {
            BitmapDrawable img = null;
            try {
                URL url = new URL(photoUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.connect();
                InputStream ins = con.getInputStream();
                img = new BitmapDrawable(getResources(), ins);
                ins.close();
                con.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return img;
        }
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            GridFragment page = GridFragment.getNewInstance(position);
            MainActivity.handlerMap.put(position,page.updater); // для тех станиц что всплывают впервые
            return page;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public void notifyDataSetChanged() {            // посылаем всем вью из списка сообщение обновления
            super.notifyDataSetChanged();
            for (Integer i : handlerMap.keySet()) {
                handlerMap.get(i).sendEmptyMessage(0);
            }
        }

    }

}