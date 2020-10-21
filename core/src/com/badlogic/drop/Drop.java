package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {
    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;

    /*
     * camera will be used to ensure we can render our target resolution 800x480
     * no matter what the actual screen resolution is.
     */
    private OrthographicCamera camera;

    //this is a special class that is used to draw 2D images.
    private SpriteBatch batch;

    private Rectangle bucket;

    //helps to transform the touch/mouse coordinate to the camera's coordinate system.
    private Vector3 touchPos;

    private Array<Rectangle> rainDrops;
    private long lastDropTime;

    @Override
    public void create() {
        //load the images for the droplet and the bucket, 64x64 pixels each
        dropImage = new Texture(Gdx.files.internal("drop.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        //load the drop and sound effect and the rain background music
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        //start the playback of the background music immediately
        rainMusic.setLooping(true);
        rainMusic.play();

        //create the camera and the SpriteBatch
        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        /*
         * this will make sure the camera always shows us
         * an area of our game world that is 800x480 units wide.
         */
        camera.setToOrtho(false, 800, 480);

        //create a Rectangle to logically represent the bucket
        bucket = new Rectangle();

        //screen center in horizontally
        bucket.x = 800 / 2 - 64 / 2;

        /*
         * 20 px above from bottom edge of the screen.
         * by default, all rendering in libGDX is performed with the y-axis pointing upwards.
         */
        bucket.y = 20;

        bucket.width = 64;
        bucket.height = 64;

        //create the raindrops array.
        rainDrops = new Array<Rectangle>();
    }

    @Override
    public void render() {
        //clear the screen with a dark blue color.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        /*
        * update camera's matrices.
        * in our sample we don't change the position of the camera.
        * but normally, this must called when the position of the camera is changed.
        */
        camera.update();

        //tell the SpriteBatch to render the coordinate system
        //specified by the camera
        batch.setProjectionMatrix(camera.combined);

        //begin a new batch and
        //draw the bucket and all drops.
        batch.begin();
        batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle rainDrop : rainDrops) {
            batch.draw(dropImage, rainDrop.x, rainDrop.y);
        }
        batch.end();

        //if the user touches the screen or presses a mouse button
        if (Gdx.input.isTouched()) {
            touchPos = new Vector3();

            //get the current touch/mouse coordinate.
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);

            //transfer touch/mouse coordinate to the camera's coordinate system.
            camera.unproject(touchPos);

            //change the bucket's x-axis position to be centered around the touch/mouse coordinates.
            bucket.x = touchPos.x - 64 / 2;
        }

        //if the user wants to control bucket position with the keyboard.
        //500 indicates the acceleration of the bucket during keyboard is pressed.
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 500 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 500 * Gdx.graphics.getDeltaTime();

        //Make sure the bucket stays within the screen limits.
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > 800 - 64) bucket.x = 800 - 64;

        //spawn a new raindrop after a certain time slot.
        if (TimeUtils.nanoTime() - lastDropTime > 700000000) spawnRaindrop();

        for (Iterator<Rectangle> iterator = rainDrops.iterator(); iterator.hasNext(); ) {
            Rectangle rainDrop = iterator.next();

            //100 indicates the speed of raindrops.
            rainDrop.y -= 300 * Gdx.graphics.getDeltaTime();

            //Raindrop is beneath the bottom edge of the screen, remove it from the array.
            if (rainDrop.y + 64 < 0) iterator.remove();

            if (rainDrop.overlaps(bucket)) {
                dropSound.play();
                iterator.remove();
            }
        }
    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();
    }

    private void spawnRaindrop() {
        Rectangle rainDrop = new Rectangle();
        rainDrop.x = MathUtils.random(0, 800 - 64);
        rainDrop.y = 480;
        rainDrop.width = 64;
        rainDrop.height = 64;
        rainDrops.add(rainDrop);
        lastDropTime = TimeUtils.nanoTime();
    }
}
