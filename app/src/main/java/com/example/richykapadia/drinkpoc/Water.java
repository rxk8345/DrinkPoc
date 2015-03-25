package com.example.richykapadia.drinkpoc;

import android.opengl.GLES20;

import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleSystem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by richykapadia on 3/23/15.
 */
public class Water {

    private int mPositionHandle;
    private int mColorHandle;
    private final int mProgram;
    private final String waterShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private ByteBuffer mParticleColorBuffer;
    private ByteBuffer mParticlePositionBuffer;
    private ByteBuffer mParticleWeightBuffer;

    private List<ParticleGroup> mParticleRenderList =
            new ArrayList<ParticleGroup>(256);

    public Water(){

        mParticlePositionBuffer = ByteBuffer
                .allocateDirect(2 * 4 * DrinkRenderer.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
        mParticleColorBuffer = ByteBuffer
                .allocateDirect(4 * DrinkRenderer.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
        mParticleWeightBuffer = ByteBuffer
                .allocateDirect(4 * DrinkRenderer.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());

        int waterShader = DrinkRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, waterShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, waterShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    public void draw() {

        ParticleSystem ps = DrinkRenderer.getInstance().acquireParticleSystem();
        int worldParticleCount = ps.getParticleCount();
        Vec2[] posArray = ps.getParticlePositionBuffer();

        GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER );
        int particleCount = ps.getParticleCount();
        int instanceOffset = ps.getBufferIndex();
        GLES20.glDrawArrays( GLES20.GL_POINTS, instanceOffset, particleCount);
        GLES20.glDisableVertexAttribArray(0);

    }



}