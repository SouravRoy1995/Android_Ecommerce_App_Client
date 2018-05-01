package edmt.dev.androidecommerceapp.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import edmt.dev.androidecommerceapp.Model.User;

/**
 * Created by User on 1/4/2018.
 */

public class Common {

    public static User currentUser;

    public static String convertCodeToStatus(String status) {

        if(status.equals("0"))
            return "Placed";

        else if(status.equals("1"))
            return "Placed,1 day to shipped";

        else if(status.equals("2"))
            return "Placed,2 day to shipped";

        else if(status.equals("3"))
            return "Placed,1 week to shipped";

        else if(status.equals("4"))
            return "Placed,2 week to shipped";

        else if(status.equals("5"))
            return "On my way,1 hour to shipped";

        else if(status.equals("6"))
            return "On my way,2 hour to shipped";

        else if(status.equals("7"))
            return "On my way,3 hour to shipped";

        else if(status.equals("8"))
            return "On my way,4 hour to shipped";

        else if(status.equals("9"))
            return "On my way, 6 hour to shipped";

        else if(status.equals("10"))
            return "On my way,10 hour to shipped";

        else
            return "Shipped";
    }

    public static final String DELETE = "Delete";

    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";


    public static boolean isConnectedToInterner(Context context)
    {
        ConnectivityManager  connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info != null)
            {
               for(int i=0;i<info.length;i++)
               {
                   if(info[i].getState() == NetworkInfo.State.CONNECTED)
                       return true;
               }
            }
        }

        return false;
    }
}
