package com.example.glapplication.app;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.opengl.GLU;
import android.os.SystemClock;
import android.util.Log;
import android.view.Window;

import com.example.glapplication.app.util.Util;
import com.example.glapplication.app.util.Square;
import com.example.glapplication.app.util.Triangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by darkstarx on 23.06.14.
 */
public class MyGLRendererExt extends MyGLRenderer {

    private int mWidth;
    private int mHeight;

    private int mSimpleProgramHandle;
    private int mTexProgramHandle;

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] mProjectionMatrix = new float[16];

    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;

    /** This will be used to pass in model color information. */
    private int mColorHandle;

    /** */
    private int mTexUniformHandle;

    /** */
    private int mTexCoordHandle;

    /** An object Square to be rendered */
    private Square square;

    /** An object Triangle to be rendered */
    private Triangle triangle;


    public MyGLRendererExt(Window window)
    {
        super(window);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglc) {
        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        if (!createShaders())
            throw new RuntimeException("Error creating programs.");

        useProgram(mSimpleProgramHandle);

        super.onSurfaceCreated(gl10, eglc);
    }

    @Override
    public void onCreate(int width, int height, boolean is_tablet) {
        mWidth = width;
        mHeight = height;

        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

        square = new Square(mWidth, mHeight);
        triangle = new Triangle();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        triangle.draw(mPositionHandle, mColorHandle, mMVPMatrixHandle, mModelMatrix, mViewMatrix, mProjectionMatrix);

        // Draw one translated a bit down and rotated to be flat on the ground.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        triangle.draw(mPositionHandle, mColorHandle, mMVPMatrixHandle, mModelMatrix, mViewMatrix, mProjectionMatrix);

        // Draw one translated a bit to the right and rotated to be facing to the left.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        triangle.draw(mPositionHandle, mColorHandle, mMVPMatrixHandle,
                mModelMatrix, mViewMatrix, mProjectionMatrix);

        useProgram(mTexProgramHandle);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 0.1f, 0.0f, 1.0f, 0.0f);
        square.draw(mPositionHandle, mColorHandle, mMVPMatrixHandle, mTexUniformHandle, mTexCoordHandle,
                mModelMatrix, mViewMatrix, mProjectionMatrix);

        useProgram(mSimpleProgramHandle);

//        Log.i(Util.LOG_TAG, "renderer: draw frame");
    }

    @Override
    public boolean swap_required() {
        return true;
    }

    private boolean createShaders() {
        final String simpleVertexShader =
                "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
                        + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.
                        + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
                        + "void main()                    \n"		// The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"		// Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        final String simpleFragmentShader =
                "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
              // precision in the fragment shader.
              + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the
              // triangle per fragment.
              + "void main()                    \n"		// The entry point for our fragment shader.
              + "{                              \n"
              + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.
              + "}                              \n";

        final String texVertexShader =
                "uniform mat4 u_MVPMatrix;         \n"	   // A constant representing the combined model/view/projection matrix.
              + "attribute vec2 a_TexCoordinate;   \n"     // Per-vertex texture coordinate information we will pass in.
              + "varying vec2 v_TexCoordinate;     \n"     // This will be passed into the fragment shader.
              + "attribute vec4 a_Position;        \n"	   // Per-vertex position information we will pass in.
              + "attribute vec4 a_Color;           \n"	   // Per-vertex color information we will pass in.
              + "varying vec4 v_Color;             \n"	   // This will be passed into the fragment shader.
              + "void main()                       \n"	   // The entry point for our vertex shader.
              + "{                                 \n"
              + "   v_Color = a_Color;             \n"	   // Pass the color through to the fragment shader.
                // It will be interpolated across the triangle.
              + "   gl_Position = u_MVPMatrix      \n"     // gl_Position is a special variable used to store the final position.
              + "               * a_Position;      \n"     // Multiply the vertex by the matrix to get the final point in
              + "v_TexCoordinate = a_TexCoordinate;\n"
              + "}                                 \n";    // normalized screen coordinates.

        final String texFragmentShader =
                "precision mediump float;       \n"	    // Set the default precision to medium. We don't need as high of a
              // precision in the fragment shader.
              + "varying vec4 v_Color;          \n"	    // This is the color from the vertex shader interpolated across the
              // triangle per fragment.
              + "uniform sampler2D u_Texture;   \n"     // The input texture.
              + "varying vec2 v_TexCoordinate;  \n"     // Interpolated texture coordinate per fragment.
              + "void main()                    \n"	    // The entry point for our fragment shader.
              + "{                              \n"
              + "   gl_FragColor = (v_Color * texture2D(u_Texture, v_TexCoordinate));\n"	    // Pass the color directly through the pipeline.
              + "}                              \n";


        mSimpleProgramHandle = createProgram(simpleVertexShader, simpleFragmentShader,
                new String[] { "a_Position", "a_Color" });
        mTexProgramHandle = createProgram(texVertexShader, texFragmentShader,
                new String[] { "a_Position", "a_Color", "a_TexCoordinate" });
        final boolean is_ok = mSimpleProgramHandle != 0 && mTexProgramHandle != 0;
        if (mSimpleProgramHandle == 0)
            Log.e(Util.LOG_TAG, "Couldn't create simple shader program");
        if (mTexProgramHandle == 0)
            Log.e(Util.LOG_TAG, "Couldn't create texture shader program");
        return is_ok;
    }

    private int createProgram(String v_shader, String f_shader, String[] attributes) {
        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        if (vertexShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, v_shader);
            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);
            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }
        if (vertexShaderHandle == 0)
        {
            Log.e(Util.LOG_TAG, "Error creating vertex shader.");
            return 0;
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if (fragmentShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, f_shader);
            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);
            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }
        if (fragmentShaderHandle == 0)
        {
            final int e = GLES20.glGetError();
            final String eStr = GLU.gluErrorString(e);
            Log.e(Util.LOG_TAG, "Error creating fragment shader: " + eStr);
            return 0;
        }

        // Create a program object and store the handle to it.
        int program = GLES20.glCreateProgram();
        if (program != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(program, vertexShaderHandle);
            // Bind the fragment shader to the program.
            GLES20.glAttachShader(program, fragmentShaderHandle);
            // Bind attributes
            int attr_id = 0;
            for (String attr : attributes) {
                GLES20.glBindAttribLocation(program, attr_id, attr);
                attr_id += 1;
            }
            // Link the two shaders together into a program.
            GLES20.glLinkProgram(program);
            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    void useProgram(int program) {
        // Get program attributes and uniforms
        mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(program, "a_Color");

        mTexUniformHandle = GLES20.glGetUniformLocation(program, "u_Texture");
        mTexCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoordinate");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(program);
    }
}
