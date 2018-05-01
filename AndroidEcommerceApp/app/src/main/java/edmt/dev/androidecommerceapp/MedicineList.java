package edmt.dev.androidecommerceapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import edmt.dev.androidecommerceapp.Common.Common;
import edmt.dev.androidecommerceapp.Database.Database;
import edmt.dev.androidecommerceapp.Interface.ItemClickListener;
import edmt.dev.androidecommerceapp.Model.Medicine;
import edmt.dev.androidecommerceapp.ViewHolder.MedicineViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MedicineList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference medicineList;

    String categoryId="";

    FirebaseRecyclerAdapter<Medicine,MedicineViewHolder> adapter;

    //Search Functionality
    FirebaseRecyclerAdapter<Medicine,MedicineViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites
    Database localDB;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Note: add this code before setContentView method
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/product.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_medicine_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        medicineList = database.getReference("Medicines");

        //Lccal DB
        localDB = new Database(this);


        recyclerView = (RecyclerView)findViewById(R.id.recycler_medicine);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Get Intent Here
        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");

        if(!categoryId.isEmpty() && categoryId != null)
        {
            if(Common.isConnectedToInterner(getBaseContext()))
                loadListMedicine(categoryId);
            else
            {
                Toast.makeText(MedicineList.this, "Please Check Your Connection !!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //Search
        materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your product name");
       // materialSearchBar.setSpeechMode(false); // No need bcz we already define it in XML

        loadSuggest(); //write function to load suggest from Firebase

        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                //When user type their text,we will change suggest list

                List<String> suggest = new ArrayList<String>();
                for(String search:suggestList) //loop is suggest list
                {
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                           suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When search bar is close
                //Restore orginal  adapter
                if(!enabled)
                    recyclerView.setAdapter(adapter);

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When search finish
                //Show result if search adapter
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });


    }

    private void startSearch(CharSequence text) {
        searchAdapter = new FirebaseRecyclerAdapter<Medicine, MedicineViewHolder>(
                Medicine.class,
                R.layout.medicine_item,
                MedicineViewHolder.class,
                medicineList.orderByChild("name").equalTo(text.toString()) //Compare Name chage -> Name


        ) {
            @Override
            protected void populateViewHolder(MedicineViewHolder viewHolder, Medicine model, int position) {

                viewHolder.medicine_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.medicine_image);

                final Medicine local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start New Activity
                        Intent medicineDetail = new Intent(MedicineList.this,MedicineDetail.class);
                        medicineDetail.putExtra("medicineId",searchAdapter.getRef(position).getKey()); // Send Medicine To New Activity change -> MedicineId
                        startActivity(medicineDetail);
                    }
                });

            }
        };
        recyclerView.setAdapter(searchAdapter); // Set adapter for Recycler View is Search result

    }

    private void loadSuggest() {
        medicineList.orderByChild("menuId").equalTo(categoryId) // change -> MenuId
                  .addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(DataSnapshot dataSnapshot) {
                          for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                          {
                              Medicine item = postSnapshot.getValue(Medicine.class);
                              suggestList.add(item.getName()); //Add name of medicine to suggest list
                          }
                      }

                      @Override
                      public void onCancelled(DatabaseError databaseError) {

                      }
                  });

    }

    private void loadListMedicine(String categoryId) {

        adapter = new FirebaseRecyclerAdapter<Medicine, MedicineViewHolder>(Medicine.class,
                R.layout.medicine_item,
                MedicineViewHolder.class,
                medicineList.orderByChild("menuId").equalTo(categoryId) // like : Select *from Medicines where MenuId change -> MenuId
              ) {
            @Override
            protected void populateViewHolder(final MedicineViewHolder viewHolder, final Medicine model, final int position) {
                viewHolder.medicine_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.medicine_image);

                //Add Favorites
                if(localDB.isFavorite(adapter.getRef(position).getKey()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //Click to change state to favorites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!localDB.isFavorite(adapter.getRef(position).getKey()))
                        {
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(MedicineList.this, ""+model.getName()+" was added to Favorites", Toast.LENGTH_SHORT).show();
                        }

                        else

                        {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(MedicineList.this, ""+model.getName()+" was removed to Favorites", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                final Medicine local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start New Activity
                        Intent medicineDetail = new Intent(MedicineList.this,MedicineDetail.class);
                        medicineDetail.putExtra("medicineId",adapter.getRef(position).getKey()); // Send Medicine To New Activity change -> MedicineId
                        startActivity(medicineDetail);
                    }
                });
            }
        };


        recyclerView.setAdapter(adapter);

    }
}
