package mylayouts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Random;

import ru.taximaster.testapp.MainActivity;
import ru.taximaster.testapp.R;
import ru.taximaster.testapp.ZoomView;

import static ru.taximaster.testapp.MainActivity.TAG;

public class GridFragment extends Fragment {

    int bkgColor;
    int page;
    final static String PAGE = "page";
    public MyAdapter adapter = new MyAdapter();
    public Handler updater = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
        }
    };

    public static GridFragment getNewInstance(int page) {
        GridFragment gf = new GridFragment();
        Bundle args = new Bundle();
        args.putInt(PAGE, page);
        gf.setArguments(args);
        return gf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Random rnd = new Random();
        bkgColor = Color.argb(40, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        page = getArguments().getInt(PAGE);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.grid_fragment, null);
        view.setBackgroundColor(bkgColor);
        ((GridView) view.findViewById(R.id.grid)).setAdapter(adapter);
        // added sim
        // актуализируен хандлеры после востановления состояния
        MainActivity.handlerMap.put(page,updater);          // например поворота или перехода в другое приожение
        GridView gridView = (GridView) view.findViewById(R.id.grid); // поиск GridView для установки OnItemClickListener
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // next string added for large view
                Bitmap picture = MainActivity.bitmaps[(int)id].getBitmap();
                Intent intent = ZoomView.newIntent(getActivity(), picture);
                startActivity(intent);
                // added sim for debug begin
                //String idStr = String.valueOf(id);
                //Toast.makeText(getContext(),idStr,Toast.LENGTH_LONG).show();
                // added sim for debug end
            }
        });
        return view;
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return MainActivity.PPP;
        }

        @Override
        public Object getItem(int i) {
            return MainActivity.bitmaps[i + MainActivity.PPP * page];
        }

        @Override
        public long getItemId(int i) {
            return i + MainActivity.PPP * page;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int ind = i + MainActivity.PPP * page;
            ImageView imageView;
            if (view != null) {
                imageView = (ImageView) view;
            } else {
                imageView = new ImageView(getActivity());
            }
            //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(8, 8, 8, 8);

            if (MainActivity.bitmaps != null) {
                try {
                    BitmapDrawable[] bs = MainActivity.bitmaps;
                    BitmapDrawable b = bs[ind];
                    imageView.setImageDrawable(b);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка показа изображения");
                }
            } else {
                //imageView.setImageResource(R.drawable.iv_ico); // clean view is better
            }

            return imageView;
        }
    }

}
