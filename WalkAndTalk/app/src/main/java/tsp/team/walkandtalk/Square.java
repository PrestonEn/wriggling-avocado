package tsp.team.walkandtalk;

import android.content.Context;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import android.opengl.GLES20;
import android.util.Log;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Square extends Sprite{

    private float height,width;
    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int mTexCoordHandle;
    private EnemyKillGesture killGesture = null;
    private TextureInfo tInfo;
    private boolean wiggle;
    private boolean wiggleDir = true;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private float squareCoords[];
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    /**
     * Square constructor. A little ugly, but that's life.
     * @param sc    coordinates of verticies on window
     * @param posX  center x position
     * @param posY  center y position
     * @param velX  velocity x component
     * @param velY  velocity y component
     * @param ctx   Context
     * @param rot   does it rotate?
     * @param a     angle of rotation
     * @param aR    delta Angle
     * @param sw    screen width
     * @param tInfo Texture info
     */
    public Square(float sc[], float posX,float posY,float velX,float velY,Context ctx,
                  boolean rot, float a, float aR, float sw, TextureInfo tInfo) {
        this.live = true;
        this.ScreenWidth = sw;
        this.rotate = rot;
        this.squareCoords = sc;
        this.tInfo = tInfo;
        this.angle = a;
        this.angleRate = aR;
        this.px = posX;
        this.py = posY;
        this.vx = velX;
        this.vy = velY;

        animUVs = new float[]{ // Invisible texture.
                0.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 0.0f,
        };

        squareCoords = sc;

        this.width = squareCoords[0] - squareCoords[6];
        this.height = squareCoords[1] - squareCoords[4];

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


        // prepare shaders and OpenGL program
        int vertexShader = GLES20Renderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                Sprite.vs_Image);

        int fragmentShader = GLES20Renderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                Sprite.fs_Image);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("linker error: ", "Could not link program: ");
            Log.e("linker error: ", GLES20.glGetProgramInfoLog(mProgram));
            GLES20.glDeleteProgram(mProgram);

        }
    }

    /**
     * This method will allow us to change a sprite/square's texture on the fly between draw methods.
     * @param tInfo TextureInfo object to set this objects texture drawing abilities to.
     */
    public void settInfo(TextureInfo tInfo) {
        this.tInfo = tInfo;
    }

    /**
     * Standard getter for width.
     * @return Float width.
     */
    public float getWidth() {
        return this.width;
    }

    /**
     * Standard getter for height.
     * @return Float height.
     */
    public float getHeight() {
        return this.height;
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(mProgram, "a_texCoord");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);

        FloatBuffer textureBuffer;
        ByteBuffer bb = ByteBuffer.allocateDirect(animUVs.length  * 4);
        bb.order(ByteOrder.nativeOrder());
        textureBuffer = bb.asFloatBuffer();
        textureBuffer.put(animUVs);
        textureBuffer.position(0);

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
            false, 0, textureBuffer);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        //get handle to fragment shader texture coordinate member
        mTexCoordHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

        // Set the sampler texture unit to the binding, where we have saved the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tInfo.getBinding());
        GLES20.glUniform1i(mTexCoordHandle, 0);

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    /**
     * set the flag to wiggle mode.
     */
    public void setWiggle(boolean wigglewiggleyeah){
        this.wiggle = wigglewiggleyeah;
    }

    /**
     * Method for returning what type of gesture will kill this object.
     * @return EnemyKillGesture enumeration type.
     */
    @Override
    public EnemyKillGesture getKillGesture(){
        return this.killGesture;
    }

    /**
     * Sets the kill gesture of a square. This is really only to be set if the square that is built is
     * an enemy of fly or run type.
     * @param killGesture EnemyKillGesture to set as.
     */
    @Override
    public void setKillGesture(EnemyKillGesture killGesture){
        this.killGesture = killGesture;
    }

    /**
     * Boolean if you need to deal with rotating or not.
     * @return Boolean need to rotate.
     */
    @Override
    public boolean needRotate() {
        return this.rotate;
    }

    /**
     * Extremely important method. Meant to update particle position and angle for global animation.
     */
    @Override
    public void updateShape() {
        this.px += this.vx;
        this.py += this.vy;
        this.angle += this.angleRate;

        if(this.wiggle && Math.abs(0.0f - angle) > 50.0f){
            this.angleRate = this.angleRate * -1f;
        }


        if(Math.abs(this.py) > 2.5)this.live = false;
        if(Math.abs(this.px) > this.ScreenWidth*2)this.live = false;
    }

    /**
     * Customize the verticies in the vertex buffer.
     * @param newVerts Float array representation to pass in.
     */
    public void setShapeVertexs(float[] newVerts){
        this.vertexBuffer.clear();
        this.vertexBuffer.put(newVerts);
        this.vertexBuffer.position(0);
    }
}