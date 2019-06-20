package com.example.andrew.doctorschatsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class GalleryMainActivity extends AppCompatActivity {

    private RecyclerView images_recyclerView;
    private ImageView gallery_placeHolder_image ;

    private FirebaseRecyclerAdapter<ImageModel , CategoryViewHolder> adapter ;

    private String ChatWithID ,SenderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_main);

        SenderID = Objects.requireNonNull(getIntent().getExtras()).getString("SenderID");
        ChatWithID = Objects.requireNonNull(getIntent().getExtras()).getString("ChatWithID");

        initializeFields();

        adapter.startListening();
        images_recyclerView.setAdapter(adapter);
    }

 /*   @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }*/

    private void initializeFields() {
        images_recyclerView = (RecyclerView)findViewById(R.id.galleryMain_RecyclerView);
        images_recyclerView.setHasFixedSize(true);

        images_recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),3));

        gallery_placeHolder_image = (ImageView)findViewById(R.id.gallery_placeHolder_image);

        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference().child("ImageMessages")
                .child(ChatWithID).child(SenderID);

        Query ImagesQuery = imagesRef.orderByChild("imgLink");
        FirebaseRecyclerOptions<ImageModel> options = new FirebaseRecyclerOptions.Builder<ImageModel>()
                .setQuery(ImagesQuery, ImageModel.class).build();

        adapter = new FirebaseRecyclerAdapter<ImageModel, CategoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CategoryViewHolder holder, int position, @NonNull final ImageModel model) {

                if(gallery_placeHolder_image.getVisibility()==View.VISIBLE)
                    gallery_placeHolder_image.setVisibility(View.GONE);

                Picasso.get().load(model.getImgLink()).placeholder(R.drawable.loading)
                        .error(R.drawable.no_image).into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
                holder.imageDate.setText(model.getImgDate());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GalleryMainActivity.this, ImageFullScreenActivity.class);
                        intent.putExtra("ImageUri" , model.getImgLink());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_category_item , viewGroup , false) ;
                return new CategoryViewHolder(view);
            }
        };
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView imageDate ;
        ImageView image ;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            imageDate = (TextView) itemView.findViewById(R.id.galleryImageDate);
            image = (ImageView)itemView.findViewById(R.id.galleryImage);
        }
    }
}
