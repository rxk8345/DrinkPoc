package lastone;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import shadercode.FragmentShaders;
import shadercode.VertexShaders;

import lastone.util.ShaderHelper;
import lastone.util.TextureHelper;

import com.google.fpl.liquidfun.EdgeShape;
import com.google.fpl.liquidfun.FixtureDef;
import com.google.fpl.liquidfun.Shape;
import com.google.fpl.liquidfun.Transform;
import com.lastone.lastoneGen.R;


import com.google.fpl.liquidfun.Body;
import com.google.fpl.liquidfun.BodyDef;
import com.google.fpl.liquidfun.ParticleFlag;
import com.google.fpl.liquidfun.ParticleGroupDef;
import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.ParticleSystemDef;
import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.World;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class LastOneRenderer implements Renderer {

    //world and particlesystem are for physics engine, reentrant lock
    //to make sure safe access to world and particlesystem
    private static World mWorld = null;
    private static Lock worldLock = new ReentrantLock();
    private static ParticleSystem mParticleSystem = null;

    private Body boundaryBody;
    private Body glassBody;
    private Body consumedBody;

    //define arguments for world.step()
    //call world.step from onDrawFrame so it gets called once per render
    private static final float TIME_STEP = 1 / 20f;
    private static final int VELOCITY_ITERATIONS = 4;
    private static final int POSITION_ITERATIONS = 2;
    private static final int PARTICLE_ITERATIONS = 3;


    //define arguments for ParticleSystemDef/ParticleSystem
    public static int MAX_PARTICLE_COUNT = 5000;
    public static float PARTICLE_RADIUS = 0.7f;
    public static final float PARTICLE_REPULSIVE_STRENGTH = 0.6f;

    private FloatBuffer particlePositionBuffer;
    private static int particleCount;

    //2D application so two positions(x,y) per component
    //variables for openGL vertex data iteration
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private final FloatBuffer vertexData;
    private Context context;

    //glslShaders and programs
    private int textureVertexShader;
    private int textureFragmentShader;
    private int particleVertexShader;
    private int particleFragmentShader;

    private int programTexture;
    private int programParticle;
    private int textureBackground;

    //user defined variables
    private int particleSizeModifier;
    private float particleOnTouchModifier;
    private float particleLifetimeModifier;

    private boolean touchAvailable = true;
    private int touchCounter = 0;

    //variables for accessing program texture variable
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private int uTextureUnitLocation;
    private static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    private int aTextureCoordinatesLocation;
    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;

    //access programParticle variables
    private int aParticlePositionLocation;
    private static final String U_COLOR = "u_Color";
    private int uColorLocation;

    private int x = 0;
    private int y;


    //##### CONSTRUCTOR ##########
    public LastOneRenderer(Context context, int numParticles, int particleSize, int particleOnTouch, int particleLifetime) {
        this.context = context;

        //set up user defined variables
        MAX_PARTICLE_COUNT = numParticles;
        PARTICLE_RADIUS = setParticleRadius(particleSize);
        particleSizeModifier = particleSize;
        particleOnTouchModifier = particleOnTouch;
        particleLifetimeModifier = particleLifetime;
        y = -10;

        float[] backgroundVertices = {
                -1f, -1f, 0f, 0f,
                -1f, 1f, 0f, 1f,
                1f, 1f, 1f, 1f,

                -1f, -1f, 0f, 0f,
                1f, 1f, 1f, 1f,
                1f, -1f, 1f, 0f

        };

        //define a floatbuffer to hold all the vertices for background triangles
        vertexData = ByteBuffer.allocateDirect(backgroundVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(backgroundVertices);

        //setup color values array, this will provide color for particles
    }

    //########### OPENGL RENDERER FUNCTIONS ###############
    @Override
    public void onDrawFrame(GL10 arg0) {
        // TODO Auto-generated method stub
        glClear(GL_COLOR_BUFFER_BIT);

        //inhibit touches so it doesnt put two down at once causing them to blow eachother away
        if (!touchAvailable) {
            touchCounter++;
            if (touchCounter > 7) {
                touchAvailable = true;
            }
        }


        //check for drinks
        PolygonShape drinkingArea = new PolygonShape();
        drinkingArea.setAsBox(25f, 4f, 0f, 23f, 0);
        int prev = mParticleSystem.getParticleCount();


        int curr = mParticleSystem.getParticleCount();
        if( prev != curr ){
            System.out.println(prev - curr);
        }

        //DRAW BACKGROUND
        glUseProgram(programTexture);
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        glEnableVertexAttribArray(aPositionLocation);
        vertexData.position(2);
        glVertexAttribPointer(aTextureCoordinatesLocation, TEXTURE_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);

        //apply texture to background
        GLES20.glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureBackground);
        GLES20.glUniform1i(uTextureUnitLocation, 0);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        //#############
        //DRAW PARTICLES
        //will need to use a different shader program from texture shader
        getParticleLocations();

        particlePositionBuffer.position(0);

        particlePositionBuffer.position(0);
        glUseProgram(programParticle);
        glEnableVertexAttribArray(aParticlePositionLocation);
        glVertexAttribPointer(aParticlePositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, particlePositionBuffer);

        glUniform4f(uColorLocation, 0.3f, 0.3f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 0, particleCount);

        stepWorld();


        if (numSpawn < 5) {
            createParticles(x, y);
            y += 10;
            numSpawn++;
        }

    }

    static int numSpawn = 0;

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        // TODO Auto-generated method stub
        glViewport(0, 0, width, height - 400);

        System.out.println("Screen Dimensions: ");
        System.out.println("Width: " + width);
        System.out.println("Height: " + height);

    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        // TODO Auto-generated method stub
        glClearColor(0f, 0f, 0f, 0f);


        //compile shaderCode
        textureVertexShader = ShaderHelper.compileVertexShader(VertexShaders.texture_vertex_shader);
        textureFragmentShader = ShaderHelper.compileFragmentShader(FragmentShaders.texture_fragment_shader);

        //compile particle shader code
        //particleVertexShader = ShaderHelper.compileVertexShader(VertexShaders.simple_vertex_shader);
        particleVertexShader = ShaderHelper.compileVertexShader(setParticleVertexShader(particleSizeModifier));

        particleFragmentShader = ShaderHelper.compileFragmentShader(FragmentShaders.simple_fragment_shader);

        //link texture and particle shader programs
        programParticle = ShaderHelper.linkProgram(particleVertexShader, particleFragmentShader);
        programTexture = ShaderHelper.linkProgram(textureVertexShader, textureFragmentShader);


        //get program attribute and uniform locations for texture program
        uTextureUnitLocation = glGetUniformLocation(programTexture, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(programTexture, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(programTexture, A_TEXTURE_COORDINATES);

        //get attribute position for particle program
        aParticlePositionLocation = glGetAttribLocation(programParticle, A_POSITION);
        uColorLocation = glGetUniformLocation(programParticle, U_COLOR);

        //get ID for image
        textureBackground = TextureHelper.loadTexture(context, R.drawable.beer_glass);

        //initialize world and particle system then create boundaries
        initWorld();

        createBoundaries();

        createGlass();


    }

    //########################################################################
    //########### LIQUIDFUN PARTICLE SIMULATION FUNCTIONS ################

    public World getWorld() {
        worldLock.lock();
        return mWorld;
    }

    public void releaseWorld() {
        worldLock.unlock();
    }

    public void initWorld() {
        World world = getWorld();
        try {
            mWorld = new World(0f, 1f);

            ParticleSystemDef pd = new ParticleSystemDef();
            pd.setRadius(PARTICLE_RADIUS);
            pd.setRepulsiveStrength(PARTICLE_REPULSIVE_STRENGTH);
            mParticleSystem = mWorld.createParticleSystem(pd);
            mParticleSystem.setMaxParticleCount(MAX_PARTICLE_COUNT);
        } finally {
            releaseWorld();
        }
    }

    public void stepWorld() {
        World world = getWorld();
        try {
            mWorld.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS, PARTICLE_ITERATIONS);
        } finally {
            releaseWorld();
        }
    }

    public void createBoundaries() {

        World world = getWorld();
        try {
            BodyDef bd = new BodyDef();
            PolygonShape boundaryShape = new PolygonShape();
            bd.setActive(true);
            boundaryBody = mWorld.createBody(bd);
            consumedBody = mWorld.createBody(bd);

            /*              set box params
            boundaryShape.setAsBox(hx, hy, centerX, centerY, angle)
            */

//            right
//            boundaryShape.setAsBox(0.5f, 25f, 25f, 0f, 0);
//            boundaryBody.createFixture(boundaryShape, 0.0f);
//            //left
//            boundaryShape.setAsBox(0.5f, 25f, -25f, 0f, 0);
//            boundaryBody.createFixture(boundaryShape, 0.0f);
//            //bottom
//            boundaryShape.setAsBox(25f, 0.5f, 0f, -25f, 0);
//            boundaryBody.createFixture(boundaryShape, 0.0f);

            //top
//            boundaryShape.setAsBox(25f, 0.5f, 0f, 25f, 0);
//            boundaryBody.createFixture(boundaryShape, 0.0f);

            //create boundary to catch the liquid consumed

            EdgeShape consumedShape = new EdgeShape();
            float[] consumed = {
                    16f, 22f,
                    22f,25f,
                    23f,0f,
                    23f,-20f,
                    50f, -20f,
                    50f, 25f,
                    30f, 35f,
                    0f, 25f
            };

            float prevX = consumed[0];
            float prevY = consumed[1];
            for( int i = 2; i < consumed.length; i+=2){
                float currX = consumed[i];
                float currY = consumed[i + 1];
                consumedShape.set(prevX,prevY, currX, currY);
                consumedBody.createFixture(consumedShape, 0f);
                prevX = currX;
                prevY = currY;
            }

            bd.delete();
            boundaryShape.delete();
        } finally {
            releaseWorld();
        }
    }

    public void createGlass() {
        World world = getWorld();
        try {
            BodyDef bd = new BodyDef();

            EdgeShape glassEdge = new EdgeShape();
            bd.setActive(true);
            glassBody = mWorld.createBody(bd);

            float[] glassVertexs = {
                    16f, 22f,   //top right inside of glass
                    16f, 16f,
                    15f, 12f,
                    13f,7f,
                    11f, 3f,
                    10f, 0f,
                    8f,  -4f,
                    7f, -8f,
                    7f, -14f,
                    8f,-17f,    //middle inside of glass
                    0f,-17f,
                    -9f, -17f,
                    -8, -13f,
                    -8f, -8f,
                    -9f, -5f,
                    -10f, -3f,
                    -12f, 0f,
                    -14f, 5f,
                    -16f, 9f,
                    -17f, 14f,
                    -17f, 21f,   // top left inside of glass
                    -19f, 22f,
                    -19f, 15f,
                    -18f, 9f,
                    -16f, 2f,
                    -13f, -5f,
                    -12f, -9f,
                    -13f, -15f,
                    -17f, -23f,
                    -16f, -25f, // bottom left outside
                    15f, -25f,  // bottom right outside
                    16f, -23f,
                    11f, -17f,
                    10f, -11f,
                    10f, -7f,
                    11f, 0f,
                    13f, 5f,
                    16f, 12f,
                    15f, 22f

            };

            float prevX = glassVertexs[0];
            float prevY = glassVertexs[1];
            for( int i = 2; i < glassVertexs.length; i+=2){
                float currX = glassVertexs[i];
                float currY = glassVertexs[i + 1];
                glassEdge.set(prevX,prevY, currX, currY);
                glassBody.createFixture(glassEdge, 0f);
                prevX = currX;
                prevY = currY;
            }

            //boundaryShape.setAsBox(hx, hy, centerX, centerY, angle)
            bd.delete();
            glassEdge.delete();
        } finally {
            releaseWorld();
        }
    }

    public void createParticles(float x, float y) {
        System.out.println("Touch: (" + x + ", " + y + ")");
        float tempSize = particleOnTouchModifier / 300;
        World world = getWorld();

        try {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(tempSize, tempSize * 1.5f, x, y, 0);
            ParticleGroupDef pgDef = new ParticleGroupDef();
            pgDef.setShape(shape);
            pgDef.setFlags(ParticleFlag.waterParticle);
            pgDef.setColor(80, 80, 230, 200);
            pgDef.setLifetime(particleLifetimeModifier);
            mParticleSystem.createParticleGroup(pgDef);
        } finally {
            releaseWorld();
        }
    }

    public void getParticleLocations() {
        particleCount = mParticleSystem.getParticleCount();
        ByteBuffer temp = ByteBuffer.allocateDirect(POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT * MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
        temp.position(0);
        mParticleSystem.copyPositionBuffer(0, particleCount, temp);

        particlePositionBuffer = temp.asFloatBuffer();

        particlePositionBuffer.rewind();

        int index = 0;
        float tempHold;
        //normalize coords so openGL knows how to draw
        while (particlePositionBuffer.hasRemaining()) {
            tempHold = particlePositionBuffer.get(index);
            particlePositionBuffer.put(tempHold / 25);
            index++;

        }
    }

    public void handleRotation(float x, float y) {


        //dividing by 8 to keep in range of 0-1, force goes past 8 but this gives a good indication
        //for whatever reason the X and Y axis of Sensor is flipped in liquidfun engine
        float finalY = -(y / 8);
        float finalX = -(x / 8);
        World world = getWorld();
        try {
            if (mWorld != null) {
                mWorld.setGravity(finalX, finalY);
            }
        } finally {
            releaseWorld();
        }

    }

    //call at some point to clean up simulation
    public void endSimulation() {
        World world = getWorld();
        try {
            if (boundaryBody != null) {
                boundaryBody.delete();
                boundaryBody = null;
            }
            if (glassBody != null) {
                glassBody.delete();
                glassBody = null;
            }
            if(consumedBody != null){
                consumedBody.delete();
                consumedBody = null;
            }
            if (mParticleSystem != null) {
                mParticleSystem.delete();
                mParticleSystem = null;
            }
            if (mWorld != null) {
                mWorld.delete();
                mWorld = null;
            }
        } finally {
            releaseWorld();
        }
    }

    public float setParticleRadius(int particleSize) {
        switch (particleSize) {
            case 1:
                return 0.12f;
            case 2:
                return 0.18f;
            case 3:
                return 0.24f;
            case 4:
                return 0.32f;
        }
        Log.d("PARTICLE RADIUS ERROR", "NO RADIUS FOUND IN setParticleRadius");
        return 0.12f;
    }

    public String setParticleVertexShader(int particleSize) {
        if (particleSize == 1) {
            return VertexShaders.simple_vertex_shader1;
        }
        if (particleSize == 2) {
            return VertexShaders.simple_vertex_shader2;
        }
        if (particleSize == 3) {
            return VertexShaders.simple_vertex_shader3;
        }
        if (particleSize == 4) {
            return VertexShaders.simple_vertex_shader4;
        }
        Log.d("SHADER ERROR", "NO SHADER WAS RETURNED FROM setPartleVertexShader");
        return VertexShaders.simple_vertex_shader2;
    }

    //get touch coordinates and convert to coordinates that liquidfun will recognize
    public boolean handleTouchEvent(float x, float y) {

        if (touchAvailable) {
            if (x >= 700) {
                x = (x - 700) / 700 * 26;
            } else {
                x = -26 + (x / 700 * 26);
            }
            if (y >= 350) {
                y = -(y - 350) / 350 * 25;
            } else {
                y = 25 - (y / 350 * 25);
            }
            createParticles(x, y);
            touchAvailable = false;
            touchCounter = 0;
        }
        return true;
    }


    public static float ScreenToWorldX(float x) {
        if (x >= 700) {
            x = (x - 700) / 700 * 26;
        } else {
            x = -26 + (x / 700 * 26);
        }
        return x;
    }

    public static float ScreenToWorldY(float y) {
        if (y >= 350) {
            y = -(y - 350) / 350 * 25;
        } else {
            y = 25 - (y / 350 * 25);
        }
        return y;
    }
}
