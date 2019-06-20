package com.example.andrew.doctorschatsystem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ImageFullScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_screen);

        final String  imageLink =Objects.requireNonNull(getIntent().getExtras()).getString("ImageUri");
        final ImageView imageView = (ImageView)findViewById(R.id.FullScreen_Image);

        if(imageLink.equals("default"))
            imageView.setImageResource(R.drawable.profile_image);

        else
            Picasso.get().load(imageLink).placeholder(R.drawable.no_image).into(imageView, new Callback() {
                @Override
                public void onSuccess() { }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ImageFullScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    imageView.setImageResource(R.drawable.no_image);
                }
            });

    }
}
