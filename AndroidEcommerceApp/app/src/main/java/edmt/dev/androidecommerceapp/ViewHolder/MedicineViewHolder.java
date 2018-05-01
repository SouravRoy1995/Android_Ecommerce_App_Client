package edmt.dev.androidecommerceapp.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edmt.dev.androidecommerceapp.Interface.ItemClickListener;
import edmt.dev.androidecommerceapp.R;

/**
 * Created by User on 1/4/2018.
 */

public class MedicineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


    public TextView medicine_name;
    public ImageView medicine_image,fav_image;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public MedicineViewHolder(View itemView){
        super(itemView);

        medicine_name = (TextView)itemView.findViewById(R.id.medicine_name);
        medicine_image = (ImageView)itemView.findViewById(R.id.medicine_image);
        fav_image = (ImageView)itemView.findViewById(R.id.fav);

        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view){
        itemClickListener.onClick(view,getAdapterPosition(),false);

    }
}
