package tsp.team.walkandtalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.TextView;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL renderer class. Collision detection and all drawing is done inside of this class.
 */
public class GLES20Renderer implements GLSurfaceView.Renderer{

    public Activity  mActivityContext;
    private static final String TAG = "MyGLRenderer";
    private GameStuff gamestuff;
    private SceneWrapper sceneWrapper;
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mTranslationMatrix = new float[16];
    private TextView score;
    private long prevHighScore;
    private AlertDialog.Builder endGameDialog;
    private GLES20SurfaceView surfaceView;

    //Default constructor meant to pass the gamestuff object from the surface view to here.
    public GLES20Renderer(GLES20SurfaceView surfaceView, SceneWrapper scene, TextView score, long prevHighScore, AlertDialog.Builder builder){
        // Nothing happens outside of gamestuff
        this.surfaceView = surfaceView;
        sceneWrapper = scene;
        this.score = score;
        this.prevHighScore = prevHighScore;
        endGameDialog = builder;
    }

    /**
     * Essentially this method is called after the constructor has returned.
     * @param unused opengl unused parameter.
     * @param config opengl configuration.
     */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // THIS MUST BE DONE TO KEEP WITHIN THE OPENGL THREAD
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE_MINUS_SRC_ALPHA);
        gamestuff = new GameStuff(mActivityContext, sceneWrapper, prevHighScore);
    }

    /**
     * This method is to be called whenever the screen is filed as dirty. Since we always want to
     * continuously be redrawing as defined in the SurfaceView, this is constantly called.
     * @param unused Unused opengl parameter.
     */
    @Override
    public void onDrawFrame(GL10 unused) {

        if(gamestuff.getCharacter().isDoneDying()) {
            surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            ScoresDBManager scoresDB = new ScoresDBManager(mActivityContext);
            scoresDB.addHighScoreRow(UUID.randomUUID().toString(), gamestuff.getScore(),
                    new Date().getTime(), sceneWrapper.getCharacterName(), sceneWrapper.getSceneName());
            if(gamestuff.getScore() > prevHighScore){
                endGameDialog.setMessage("NEW HIGH SCORE: " + gamestuff.getScore() + "\nDo you want to retry this level?");
            } else{
                endGameDialog.setMessage("Your Score: " + gamestuff.getScore() + "\nDo you want to retry this level?");
            }

            mActivityContext.runOnUiThread(new Runnable() {
                public void run() {
                    endGameDialog.show();
                }
            });
        }
        else {

            // Updates to the text on the screen.
            mActivityContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    score.setText("Score: " + gamestuff.getScore() + "");
                }
            });
            if(gamestuff.getCharacter().getSquare().live)gamestuff.updateScore();

            // Draw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            // Set the camera position (View matrix)
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            // Draw each sprite relative proper matrix
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

            float[] scratchb1 = new float[16];
            Matrix.setIdentityM(mTranslationMatrix, 0);
            Matrix.translateM(mTranslationMatrix, 0, gamestuff.getBackground().visibleImage.px,
                    gamestuff.getBackground().visibleImage.py, 0);
            Matrix.multiplyMM(scratchb1, 0, mMVPMatrix, 0, mTranslationMatrix, 0);


            float[] scratchb2 = new float[16];
            Matrix.setIdentityM(mTranslationMatrix, 0);
            Matrix.translateM(mTranslationMatrix, 0, gamestuff.getBackground().visibleImage2.px,
                    gamestuff.getBackground().visibleImage2.py, 0);
            Matrix.multiplyMM(scratchb2, 0, mMVPMatrix, 0, mTranslationMatrix, 0);

            float[] scratchb3 = new float[16];
            Matrix.setIdentityM(mTranslationMatrix, 0);
            Matrix.translateM(mTranslationMatrix, 0, gamestuff.getBackground().visibleImage3.px,
                    gamestuff.getBackground().visibleImage3.py, 0);
            Matrix.multiplyMM(scratchb3, 0, mMVPMatrix, 0, mTranslationMatrix, 0);

            gamestuff.getBackground().testWrap(scratchb1, scratchb2, scratchb3);


            // Draw the character and go from there.
            // See comments below inside for loop for explanation of below code.

            float[] scratch = new float[16];
            Matrix.setIdentityM(mTranslationMatrix, 0);
            Matrix.translateM(mTranslationMatrix, 0, gamestuff.getCharacter().getSquare().px,
                    gamestuff.getCharacter().getSquare().py, 0);
            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mTranslationMatrix, 0);
            gamestuff.getCharacter().getSquare().draw(scratch);
            gamestuff.getCharacter().getHitBox().draw(scratch);
            gamestuff.getCharacter().update(); // Implement this later to get it actually animating.
            gamestuff.spawnPoller();
            // Java threadsafe crystal-healing.
            for (Iterator<Sprite> iterator = gamestuff.getEnemies().iterator(); iterator.hasNext();) {
                Sprite s = iterator.next();
                if (!s.needRotate()) { // If the sprite does not have an animated rotation...
                    float[] aScratch = new float[16]; // Build a matrix to apply transformations to.
                    Matrix.setIdentityM(mTranslationMatrix, 0); // Load the identity in, aka reset it.
                    Matrix.translateM(mTranslationMatrix, 0, s.px, s.py, 0); // Apply the translation.
                    Matrix.multiplyMM(aScratch, 0, mMVPMatrix, 0, mTranslationMatrix, 0); // Multiply by MVP.
                    s.draw(aScratch); // Draw.
                }
                else { // Slightly more complicated rotation of shapes.
                    float[] aScratch = new float[16]; // Scratches.
                    float[] bScratch = new float[16];

                    Matrix.setRotateM(mRotationMatrix, 0, s.getAngle(), 0, 0, 1.0f); // Load rotate based on angle.
                    Matrix.setIdentityM(mTranslationMatrix, 0); // Reset the translation.
                    Matrix.translateM(mTranslationMatrix, 0, s.px, s.py, 0); // Apply translation to recent reset.
                    Matrix.multiplyMM(aScratch, 0, mTranslationMatrix, 0, mRotationMatrix, 0); // Multiply the rotation and translation.
                    Matrix.multiplyMM(bScratch, 0, mMVPMatrix, 0, aScratch, 0); // Scale to MVP.
                    s.draw(bScratch); // Draw.
                }
                s.updateShape(); // Make sure that the position and other things are updated.
                if(!s.live)iterator.remove(); // Shape is kill.
            }
            // Test for any collisions in the system.
            if(examineCollisions()){
                gamestuff.getCharacter().getSquare().live = false; // Character is kill.
                stopAllMovingObjects();
            }

        }
    }

    /**
     * This method is called when the character has been hit by an enemy and will turn the velocity
     * of all objects to 0.
     */
    private void stopAllMovingObjects(){
        for(Sprite s : gamestuff.getEnemies()){ // This stop all of the enemies.
            s.vx = 0;
            s.vy = 0;
            s.angleRate = 0.0f;
        }
        gamestuff.getBackground().visibleImage.vx = 0; // Stop the background from scrolling on death.
        gamestuff.getBackground().visibleImage2.vx = 0;
        gamestuff.getBackground().visibleImage3.vx = 0;
    }

    /**
     * Purpose of this method is to view the positions of all of the shapes and view if they have
     * collided with the character at all. If they have, raise some flag.
     * @return Boolean if the character has been hit.
     */
    private boolean examineCollisions(){
        Character character = gamestuff.getCharacter();

        for(Sprite s : gamestuff.getEnemies()){
            if(detectCharTouch(s, character))return true;
        }

        return false;
    }

    /**
     * Detection of collision between a particular sprite and the character of this game run.
     * @param s Sprite to check with.
     * @param c Character to check against.
     * @return Boolean if there is a collision.
     */
    private boolean detectCharTouch(Sprite s, Character c){
        return (Math.abs(s.px - c.getSquare().px) * 2 < (s.getWidth() + c.getHitBox().getWidth())) &&
                (Math.abs(s.py - c.getSquare().py) * 2 < (s.getHeight() + c.getHitBox().getHeight()));
    }

    /**
     * When the surface has changed at all, this method updates the screen.
     * @param unused opengl unused param.
     * @param width Width of the view.
     * @param height Height of the view.
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + type + ":");
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Get the GameStuff object associated with this Renderer.
     * @return GameStuff belonging to this object.
     */
    public GameStuff getGamestuff(){
        return this.gamestuff;
    }
}