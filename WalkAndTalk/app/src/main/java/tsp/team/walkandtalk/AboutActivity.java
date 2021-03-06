package tsp.team.walkandtalk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Class which represents the about page for the app.
 */
public class AboutActivity extends Activity {

    /** onCreate
     * This method overrides Activity's OnCreate method.  It calls the
     * parent's method and then sets what layout to use.  It also overrides
     * the default font with a special chalkboard font.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Typeface font;  // Chalkboard font

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        try {
            //font = Typeface.createFromAsset(getAssets(), "DK Cool Crayon.ttf");
            font = Typeface.createFromAsset(getAssets(), "EraserRegular.ttf");
        } catch (java.lang.RuntimeException e) {
            font = null;
        }

        if (font != null) { // Set all text fields to use the chalkboard font
            TextView title = (TextView) findViewById(R.id.txtTitle);
            Button back = (Button) findViewById(R.id.btnBack);
            TextView preston = (TextView) findViewById(R.id.prestonBox);
            TextView stacey = (TextView) findViewById(R.id.staceyBox);
            TextView tyler = (TextView) findViewById(R.id.tylerBox);
            Button web = (Button) findViewById(R.id.webButton);

            title.setTypeface(font);
            back.setTypeface(font);
            preston.setTypeface(font);
            stacey.setTypeface(font);
            tyler.setTypeface(font);
            web.setTypeface(font);
        }

    } // onCreate

    /** fromAboutToMain
     * When the user presses the "Back To Main Menu" button, this method will run
     * and they will be taken back to the main menu activity.
     *
     * @param v View containing information about the nature of the event
     */
    public void fromAboutToMain(View v) {

        Intent intent = new Intent(AboutActivity.this, MainMenuActivity.class);
        startActivity(intent);

    } // fromAboutToMain

    /** fromAboutToWeb
     * Method for allowing the user to externally view the website for extra "about" information.
     * @param v View containing information about the nature of the event
     */
    public void fromAboutToWeb(View v){

        String url = "http://www.cosc.brocku.ca/~pe12nh";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    } // fromAboutToWeb

    @Override
    public void onBackPressed(){
    }

} // AboutActivity