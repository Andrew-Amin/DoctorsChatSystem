package com.example.andrew.doctorschatsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private Toolbar mtoolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

       initializeFields();
    }

    private void initializeFields() {
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Consultation chat");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mtoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View view= navigationView.getHeaderView(0);
        TextView title=(TextView)view.findViewById(R.id.navtile);
        title.setText("old value");
        navigationView.setNavigationItemSelectedListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED)
        {
            getSupportActionBar().setIcon(R.drawable.ic_warning_black_24dp);
            getSupportActionBar().setTitle("   No connection");
        }
        else
        {
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setTitle("Consultation chat");
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null)
            SendUserToLogInActivity();

        else
        {
            updateUserStatus("online");
            verifyUserExistence();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
            updateUserStatus("offline");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
            updateUserStatus("offline");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
            updateUserStatus("offline");
        finish();
        moveTaskToBack(true) ;
    }

    private void verifyUserExistence() {
        final String currentUserID = firebaseAuth.getCurrentUser().getUid();
        rootRef.child("Doctors").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if (!(dataSnapshot.child("uName").exists()))
                        SendUserToSettingsActivity();
                    else
                    {
                        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                        View view= navigationView.getHeaderView(0);

                        if(dataSnapshot.hasChild("image"))
                        {
                            CircleImageView navImage = (CircleImageView)view.findViewById(R.id.nav_user_image);
                            Picasso.get().load(dataSnapshot.child("image").getValue().toString())
                                    .error(R.drawable.no_image).placeholder(R.drawable.profile_image).into(navImage);
                        }

                        TextView title=(TextView)view.findViewById(R.id.navtile);
                        title.setText(dataSnapshot.child("uName").getValue().toString().trim());

                        TextView subTitle=(TextView)view.findViewById(R.id.navsubtitle);
                        subTitle.setText(dataSnapshot.child("uStatus").getValue().toString().trim());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToLogInActivity() {
        Intent intent = new Intent(ChatMainActivity.this, log_in.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void SendUserToSettingsActivity() {
        Intent intent = new Intent(ChatMainActivity.this, SettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.menuItem_findDoctor:
                Intent intent =  new Intent(ChatMainActivity.this, FindDoctorActivity.class);
                intent.putExtra("fd_senderID" , "NULL");
                intent.putExtra("fd_chatWithID" , "NULL");
                startActivity(intent);
                return true;

            case R.id.menuItem_CreateGroup:
                RequestNewGroup();
                return true;

            case R.id.menuItem_settings:
                startActivity( new Intent(ChatMainActivity.this, SettingActivity.class));
                return true;

            case R.id.menuItem_LogOut:
                updateUserStatus("offline");
                firebaseAuth.signOut();
                Toast.makeText(ChatMainActivity.this, "consider as a pleasure to serve you", Toast.LENGTH_SHORT).show();
                SendUserToLogInActivity();
                return true;

            default:
                return false;
        }

    }

    private void RequestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatMainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");

        final EditText groupNameField = new EditText(ChatMainActivity.this);
        groupNameField.setHint("e.g New Group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName))
                    Toast.makeText(ChatMainActivity.this, "You must enter a group name", Toast.LENGTH_SHORT).show();
                else
                    createNewGroup(groupName);
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void createNewGroup(final String groupName) {
        rootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(ChatMainActivity.this, groupName + " group is created successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserStatus (String state) {
        String currentTime , currentDate ;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = timeFormat.format(calendar.getTime());

        HashMap<String , Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time" , currentTime);
        onlineStateMap.put("date" , currentDate);
        onlineStateMap.put("state" , state);

        currentUserID = firebaseAuth.getCurrentUser().getUid();

        rootRef.child("Doctors").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        switch (item.getItemId()) {

            case R.id.edit:
                startActivity(new Intent(ChatMainActivity.this, SettingActivity.class));
                break;

            case R.id.logout:
                updateUserStatus("offline");
                firebaseAuth.signOut();
                Toast.makeText(ChatMainActivity.this, "consider as a pleasure to serve you", Toast.LENGTH_SHORT).show();
                SendUserToLogInActivity();
                break;

            default:
                return false;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

