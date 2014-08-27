package com.example.glapplication.app.util;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;


/**
 * Created by darkstarx on 27.08.14.
 */
public class Square {

    /** Offset of the position data. */
    private final int mPositionOffset = 0;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;

    /** Size of the tex coord data in elements. */
    private final int mTexCoordDataSize = 2;

    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    /** How many elements per vertex. */
    private final int mStrideBytes = 7 * mBytesPerFloat;

    /** Offset of the color data. */
    private final int mColorOffset = 3;

    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] mMVPMatrix = new float[16];

    // Our vertices.
    private float vertices[] = {
        // X, Y, Z
        // R, G, B, A
        -1.0f,  1.0f, 0.0f,  // 0, Top Left
        1.0f, 1.0f, 1.0f, 1.0f,

        -1.0f, -1.0f, 0.0f,  // 1, Bottom Left
        1.0f, 1.0f, 1.0f, 1.0f,

        1.0f, -1.0f, 0.0f,  // 2, Bottom Right
        1.0f, 1.0f, 1.0f, 1.0f,

        1.0f,  1.0f, 0.0f,  // 3, Top Right
        1.0f, 1.0f, 1.0f, 1.0f
    };

    private float textureCoords[] = {
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        1.0f, 1.0f
    };

    // The order we like to connect them.
    private short[] indices = { 0, 1, 2, 0, 2, 3 };

    // Our vertex buffer.
    private FloatBuffer vertexBuffer;

    // Our index buffer.
    private ShortBuffer indexBuffer;

    // Our index buffer.
    private FloatBuffer texBuffer;

    // Texture size
    private int mWidth, mHeight;

    public Square(int width, int height) {
        // a float is 4 bytes, therefore we multiply the number if vertices with 4.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // short is 2 bytes, therefore we multiply the number if vertices with 2.
        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        // a float is 4 bytes, therefore we multiply the number if vertices with 4.
        ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoords.length * 2);
        tbb.order(ByteOrder.nativeOrder());
        texBuffer = tbb.asFloatBuffer();
        texBuffer.put(textureCoords);
        texBuffer.position(0);

        // texture size
        mWidth = width;
        mHeight = height;
    }

    /**
     * This function draws our square on screen.
     * @param positionHandle
     * @param colorHandle
     * @param MVPMatrixHandle
     * @param texHandle
     * @param texCoordHandle
     * @param modelMatrix
     * @param viewMatrix
     * @param projectionMatrix
     */
    public void draw(int positionHandle, int colorHandle, int MVPMatrixHandle, int texHandle, int texCoordHandle,
                     float[] modelMatrix, float[] viewMatrix, float[] projectionMatrix) {
        // Pass in the position information
        vertexBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(positionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Pass in the color information
        vertexBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(colorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, vertexBuffer);
        GLES20.glEnableVertexAttribArray(colorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        // Pass in the texture coords information
        GLES20.glVertexAttribPointer(texCoordHandle, mTexCoordDataSize, GLES20.GL_FLOAT, false,
                2 * mBytesPerFloat, texBuffer);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        // Prepare texture
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glCopyTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 0, 0, mWidth, mHeight, 0);

        // Pass in the texture id
        GLES20.glUniform1i(texHandle, tex[0]);

        // Draw square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Release texture
        GLES20.glDeleteTextures(1, tex, 0);
    }
}
