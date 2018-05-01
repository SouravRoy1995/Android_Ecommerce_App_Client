package edmt.dev.androidecommerceapp;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import edmt.dev.androidecommerceapp.Common.Common;
import edmt.dev.androidecommerceapp.Database.Database;
import edmt.dev.androidecommerceapp.Model.Medicine;
import edmt.dev.androidecommerceapp.Model.Order;
import edmt.dev.androidecommerceapp.Model.Rating;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MedicineDetail extends AppCompatActivity implements RatingDialogListener{

    TextView medicine_name,medicine_price,medicine_description;
    ImageView medicine_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart,btnRating;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;


    String medicineId="";

    FirebaseDatabase database;
    DatabaseReference medicines;
    DatabaseReference ratingTbl;

    Medicine currentMedicine;

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

        setContentView(R.layout.activity_medicine_detail);

        //FireBase
        database = FirebaseDatabase.getInstance();
        medicines = database.getReference("Medicines");
        ratingTbl = database.getReference("Rating");


        //Init view
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (FloatingActionButton)findViewById(R.id.btnCart);
        btnRating = (FloatingActionButton)findViewById(R.id.btn_rating);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });


        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(
                        medicineId,
                        currentMedicine.getName(),
                        numberButton.getNumber(),
                        currentMedicine.getPrice(),
                        currentMedicine.getDiscount()

                ));

                Toast.makeText(MedicineDetail.this, "Added To Cart", Toast.LENGTH_SHORT).show();
            }
        });

        medicine_description = (TextView)findViewById(R.id.medicine_description);
        medicine_name = (TextView)findViewById(R.id.medicine_name);
        medicine_price = (TextView)findViewById(R.id.medicine_price);
        medicine_image = (ImageView)findViewById(R.id.img_medicine);


        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get Medicine Id From Intent

        if(getIntent() != null)
            medicineId = getIntent().getStringExtra("medicineId"); // change -> MedicineId

        if(!medicineId.isEmpty())
        {
            if(Common.isConnectedToInterner(getBaseContext()))

            {
                getDetailMedicine(medicineId);
                getRatingMedicine(medicineId);
            }

            else
            {
                Toast.makeText(MedicineDetail.this, "Please Check Your Connection !!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }

    private void getRatingMedicine(String medicineId) {
        Query medicineRating = ratingTbl.orderByChild("medicineId").equalTo(medicineId);

        medicineRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }

                if(count != 0)
                {
                    float average = sum/count;
                    ratingBar.setRating(average);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Exellent"))
                .setDefaultRating(1)
                .setTitle("Rate this Product")
                .setDescription("Please select some star and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please enter your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(MedicineDetail.this)
                .show();

    }

    private void getDetailMedicine(String medicineId) {
        medicines.child(medicineId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentMedicine = dataSnapshot.getValue(Medicine.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentMedicine.getImage())
                        .into(medicine_image);

                collapsingToolbarLayout.setTitle(currentMedicine.getName());

                medicine_price.setText(currentMedicine.getPrice());

                medicine_name.setText(currentMedicine.getName());

                medicine_description.setText(currentMedicine.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {
        //Get rating and upload to firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                medicineId,
                String.valueOf(value),
                comments);
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Common.currentUser.getPhone()).exists())
                {
                    //Remove Old Value
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);

                }
                else
                {
                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                Toast.makeText(MedicineDetail.this, "Thank you for your submit rating !!!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
