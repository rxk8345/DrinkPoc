package com.example.richykapadia.drinkpoc;

import android.opengl.GLSurfaceView;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleSystem;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;


import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLU;

import static android.opengl.GLES20.*;


/**
 * Created by richykapadia on 3/13/15.
 */
public class DrinkRenderer implements GLSurfaceView.Renderer {

    private World mWorld = null;
    private Body groundBody;
    private ParticleGroup mParticleGroup;
    private Body mBoundaryBody = null;
    private ReentrantLock mWorldLock = new ReentrantLock();
    private static final float WORLD_HEIGHT = 3f;
    public float sRenderWorldWidth = WORLD_HEIGHT;
    public float sRenderWorldHeight = WORLD_HEIGHT;
    private static final float BOUNDARY_THICKNESS = 20.0f;
    private Body boundaryBody;

    public static final int MAX_PARTICLE_COUNT = 5000;

    private static final DrinkRenderer _instance = new DrinkRenderer();

    private ParticleSystem mParticleSystem;

    private Water mWater;



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
        gl.glClearColor(1, 1, 1, 1);
        gl.glClearDepthf(1f);

        mWater = new Water();

        reset(); //resets the drink
        //create a body of water
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        // Continuously called by the gl surface
        // Redraw background color
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        mWater.draw();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //This method is called when the view goes from landscape -> portrait or vice versa
        //Not needed for this app
    }

    private void deleteWorld() {
        World world = acquireWorld();

        try {
            if (mBoundaryBody != null) {
                mBoundaryBody = null;
            }
            if (world != null) {
                mWorld = null;
                mParticleGroup = null;
            }
        } finally {
            releaseWorld();
        }
    }

    /**
     * Resets the world -- which means a delete and a new.
     * Initializes the boundaries and reset the ParticleRenderer as well.
     */
    public void reset() {
        World world = acquireWorld();
        try {
            deleteWorld();
            mWorld = new World(new Vec2(0, 9.8f));

            initBoundaries();
            initParticleSystem();

        } finally {
            releaseWorld();
        }
    }

    /** Create a new particle system */
    private void initParticleSystem() {
        World world = acquireWorld();
        try {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(10f, 10f);

            // Create a new particle system; we only use one.
            ParticleGroupDef groupDef = new ParticleGroupDef();
            groupDef.shape = shape;
            groupDef.flags = ParticleType.b2_waterParticle;
            //color
            ParticleColor c = new ParticleColor();
            c.r = 10;
            c.g = 10;
            c.b = 80;
            c.a = 80;
            groupDef.color = c;
            mParticleGroup = mWorld.createParticleGroup(groupDef);
            mParticleSystem = new ParticleSystem(mWorld);


        } finally {
            releaseWorld();
        }
    }

    /** Constructs the ground. **/
    private void initBoundaries() {

        BodyDef bodyDef = new BodyDef();
        groundBody = acquireWorld().createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        Vec2[] vertices =
                new Vec2[] {new Vec2(-40, -10), new Vec2(40, -10), new Vec2(40, 0), new Vec2(-40, 0)};
        shape.set(vertices, 4);
        groundBody.createFixture(shape, 0.0f);

    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static DrinkRenderer getInstance(){
        return _instance;
    }

    /**
     * Acquire the world for thread-safe operations.
     */
    public World acquireWorld() {
        mWorldLock.lock();
        return mWorld;
    }

    /**
     * Release the world after thread-safe operations.
     */
    public void releaseWorld() {
        mWorldLock.unlock();
    }

    /**
     * Acquire the particle system for thread-safe operations.
     * Uses the same lock as World, as all LiquidFun operations should be
     * synchronized. For example, if we are in the middle of World.sync(), we
     * don't want to call ParticleSystem.createParticleGroup() at the same
     * time.
     */
    public ParticleSystem acquireParticleSystem() {
        mWorldLock.lock();
        return mParticleSystem;
    }

    /**
     * Release the world after thread-safe operations.
     */
    public void releaseParticleSystem() {
        mWorldLock.unlock();
    }

    /**
     * This provides access to the main Activity class that our Renderer is
     * associated with. Provided for debug access; use with care.
     */
//    public Activity getCurrentActivity() {
//        return mActivity;
//    }


}
