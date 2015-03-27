package com.example.richykapadia.myapplication;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleSystem;
import org.jbox2d.particle.ParticleType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by richykapadia on 3/25/15.
 */
public class DrinkRenderer implements GLSurfaceView.Renderer {

    //screen dimensions
    private int screenWidth;
    private int screenHeight;
    private float worldWidth;
    private float worldHeight;

    //physics
    private World world;
    private ParticleSystem particleSystem;
    private ParticleGroup particleGroup;
    private Vec2 gravity = new Vec2(0,-10);
    private Body boundary;

    protected FloatBuffer vertBuffer;
    protected FloatBuffer colorBuffer;


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        //create water body
        this.world = new World(gravity);
        ParticleGroupDef pgdef = new ParticleGroupDef();
        PolygonShape shape = new PolygonShape();
        Vec2 worldDimensions = new Vec2(this.screenWidth, this.screenHeight);
        shape.setAsBox(worldDimensions.x/5, worldDimensions.y/5);
        pgdef.shape = shape;
        pgdef.position.set(this.worldWidth/2, this.worldHeight/2);
        pgdef.flags = ParticleType.b2_waterParticle;
        pgdef.strength = 1;
        pgdef.color = new ParticleColor( new Color3f(10f,10f,255f));

//        this.world.createParticleGroup(pgdef);
        this.particleSystem = new ParticleSystem( this.world );
        this.particleGroup = this.particleSystem.createParticleGroup(pgdef);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        if(this.particleSystem != null){
            gl.glPushMatrix();
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);


            //draw square prototype
            float[] vertices = new float[12];
            float width = 1f;
            float height = 1f;
            vertices[0] = width * -0.5f;   //x0
            vertices[1] = height * -0.5f;  //y0
            vertices[2] = 1.0f;            //z0

            vertices[3] = width * -0.5f;
            vertices[4] = height * 0.5f;
            vertices[5] = 1.0f;

            vertices[6] = width * 0.5f;
            vertices[7] = height * -0.5f;
            vertices[8] = 1.0f;

            vertices[9] = width * 0.5f;
            vertices[10] = height * 0.5f;
            vertices[11] = 1.0f;

            float[] colors = new float[16];
            colors[0] = 0f;
            colors[1] = 1f;
            colors[2] = 0f;
            colors[3] = 1f;

            colors[4] = 1f;
            colors[5] = 0f;
            colors[6] = 0f;
            colors[7] = 1f;

            colors[8] = 0f;
            colors[9] = 0f;
            colors[10] = 1f;
            colors[11] = 1f;

            colors[12] = 1f;
            colors[13] = 0f;
            colors[14] = 0f;
            colors[15] = 1f;

            //setup vertex buffer
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            vertBuffer = byteBuffer.asFloatBuffer();
            vertBuffer.put(vertices);
            vertBuffer.position(0);

            //set up color buffer
            ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(colors.length * 4);
            byteBuffer2.order(ByteOrder.nativeOrder());
            colorBuffer = byteBuffer2.asFloatBuffer();
            colorBuffer.put(colors);
            colorBuffer.position(0);

            gl.glTranslatef(540, 100, 1.0f);
            // Set the angle on each axis, 0 on x and y, our angle on z
            gl.glRotatef(0.0f, 1.0f, 0.0f, 1.0f);
            gl.glRotatef(0.0f, 0.0f, 1.0f, 1.0f);
            gl.glRotatef(0, 0.0f, 0.0f, 1.0f);

            // Grab our color, convert it to the 0.0 - 1.0 range, then set it
            gl.glColor4f(0.5f, 0f, 1.0f, 1.0f);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertBuffer);
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

            /*
            Vec2 position = this.particleGroup.getPosition();
            gl.glTranslatef(position.x, position.y, 1.0f);
            // Set the angle on each axis, 0 on x and y, our angle on z
            gl.glRotatef(0.0f, 1.0f, 0.0f, 0.0f);
            gl.glRotatef(0.0f, 0.0f, 1.0f, 0.0f);
            gl.glRotatef(this.particleGroup.getAngle(), 0.0f, 0.0f, 1.0f);


            // Grab our color, convert it to the 0.0 - 1.0 range, then set it
            gl.glColor4f(50f, 50f, 255f, 1.0f);

            //Draw!
            Vec2[] vectorPos = this.particleSystem.getParticlePositionBuffer();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vectorPos.length * 4 * 2);
            byteBuffer.order(ByteOrder.nativeOrder());
            FloatBuffer posBuffer = byteBuffer.asFloatBuffer();

            for( Vec2 vec : vectorPos){
                posBuffer.put(vec.x);
                posBuffer.put(vec.y);
            }

            posBuffer.position(0);
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, posBuffer);
            gl.glDrawArrays(GL10.GL_POINTS, 0, vectorPos.length /3 );
            */

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            gl.glPopMatrix();


            this.world.step(0.016666666f, 6, 6);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //called on create and screen rotation
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glOrthof(0, width, 0, height, -10, 10);

        this.screenHeight = height;
        this.screenWidth = width;
        Vec2 worldVec = screenToWorld(new Vec2(this.screenWidth, this.screenHeight));
        this.worldWidth = worldVec.x;
        this.worldHeight = worldVec.y;

        //create boundary fixture around the screen
        BodyDef bodyDef = new BodyDef();
        PolygonShape boundaryPolygon = new PolygonShape();
        this.boundary = this.world.createBody(bodyDef);

        float thickness = 10f;

        //top
        boundaryPolygon.setAsBox(worldWidth,thickness, new Vec2(0, worldHeight/2), 0);
        boundary.createFixture(boundaryPolygon, 0f);

        //right
        boundaryPolygon.setAsBox(thickness,worldHeight, new Vec2(worldWidth/2, 0), 0);
        boundary.createFixture(boundaryPolygon, 0f);

        //bottom
        boundaryPolygon.setAsBox(worldWidth,thickness, new Vec2(0, -worldHeight/2), 0);
        boundary.createFixture(boundaryPolygon, 0f);

        //left
        boundaryPolygon.setAsBox(thickness,worldHeight, new Vec2(-worldWidth/2, 0), 0);
        boundary.createFixture(boundaryPolygon, 0f);


    }


    /**
     * Conversion
     */
    private static float PPM = 128.0f;

    public static Vec2 screenToWorld(Vec2 cords) {
        return new Vec2(cords.x / PPM, cords.y / PPM);
    }

    public static Vec2 worldToScreen(Vec2 cords) {
        return new Vec2(cords.x * PPM, cords.y * PPM);
    }

    public static float getPPM() { return PPM; }
    public static float getMPP() { return 1.0f / PPM; }
}
