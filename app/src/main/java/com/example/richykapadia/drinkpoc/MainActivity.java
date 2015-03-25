package com.example.richykapadia.drinkpoc;

import android.graphics.PixelFormat;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import org.jbox2d.dynamics.BodyDef;
import android.opengl.GLSurfaceView;



public class MainActivity extends ActionBarActivity {

    private GLSurfaceView mWorldView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrinkRenderer renderer = new DrinkRenderer();
        setContentView(R.layout.activity_main);

        mWorldView = (GLSurfaceView) findViewById(R.id.world);
        mWorldView.setRenderer(renderer);

    }

    @Override
    protected void onPause(){
        super.onPause();
        mWorldView.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mWorldView.onResume();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
