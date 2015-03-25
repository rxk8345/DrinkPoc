package com.example.richykapadia.drinkpoc;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleSystem;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by richykapadia on 3/25/15.
 */
public class DrinkRenderer implements GLSurfaceView.Renderer {

    //Screen Dimensions
    private int width;
    private int height;

    //JboxWorld
    private World world;
    private ParticleSystem particleSystem;
    private Vec2 gravity = new Vec2(0,-10);


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1,1,1,1); //sets background color on clear

        //Create World
        if(world == null){
            this.world = new World(gravity);
        }

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //called on create and rotate
        this.height = height;
        this.width = width;

    }
}
