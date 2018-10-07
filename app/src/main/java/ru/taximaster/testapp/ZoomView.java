package ru.taximaster.testapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;


public class ZoomView extends AppCompatActivity {
    private Bitmap picture;

    private static final String EXTRA_IMAGE_BITMAP_BYTEARRAY = "ru.taximaster.testapp.zoomview.BitmapByteArray";

    public static Intent newIntent(Context packageContext, Bitmap picture){
        Intent intent = new Intent(packageContext,ZoomView.class);
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.PNG, 100, bytestream);
        intent.putExtra(EXTRA_IMAGE_BITMAP_BYTEARRAY, bytestream.toByteArray());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_view);
        picture = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra(EXTRA_IMAGE_BITMAP_BYTEARRAY),0,getIntent().getByteArrayExtra(EXTRA_IMAGE_BITMAP_BYTEARRAY).length);;
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(picture);
    }
}
