package com.example.richykapadia.drinkpoc;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by richykapadia on 3/25/15.
 */
public class DrinkRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        System.out.println("When does this get called?");
    }
}
