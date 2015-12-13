package tsp.team.walkandtalk;

import android.content.Context;

/**
 * This class is responsible for building enemies for the game loop to throw at the Character.
 * Basic Factory design patter at work.
 */
public class EnemyFactory {

    private Context contextHolder; // Reference to context needed for building sprites.
    private static float screenRatio; // Stored value of width over height of the phone.
    private TextureFactory textureFactory; // Texture factory which will get our opengl textures.
    private static float stillShape[] = { // Vertices for a still enemy.
         0.5f,  0.2f, 0.0f,   // top left
         0.5f, -0.2f, 0.0f,   // bottom left
         -0.5f, -0.2f, 0.0f,   // bottom right
         -0.5f,  0.2f, 0.0f }; // top right

    /**
     * Main constructor for the object which will just build the necessary instance variables.
     * @param c Context of application.
     */
    public EnemyFactory(Context c, TextureFactory textureFactory, float screenRatio){
        this.contextHolder = c;
        this.textureFactory = textureFactory;
        this.screenRatio = screenRatio;
    }

    /**
     * Method for building a still enemy of a particular difficulty.
     * @param difficulty DifficultySetting enumeration of EASY, MEDIUM, and HARD.
     * @return Sprite form of the particular enemy type.
     */
    public Square makeStillEnemy(DifficultySetting difficulty){
        float rate = 0.0f;
        switch(difficulty){
            case DIFFICULTY_EASY:
                rate = -0.02f;
                break;
            case DIFFICULTY_MEDIUM:
                rate = -0.033f;
                break;
            case DIFFICULTY_HARD:
                rate = -0.041f;
                break;
        }

        return new Square(stillShape, 2.49f, -0.75f, rate, 0.0f, contextHolder,
            false, 0.0f, 0.0f, screenRatio, textureFactory.getScene_enemies_still());
    }

}