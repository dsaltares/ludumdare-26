package com.siondream.ld26;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.lights.box2dLight.Light;
import com.lights.box2dLight.RayHandler;

public class LudumDare extends Game {
	
	private static final String TAG = "LudumDare";
	
	private Logger logger;
	private ObjectMap<String, LudumScreen> screens;
	private LudumScreen nextScreen;
	private OrthographicCamera camera;
	private OrthographicCamera uiCamera;
	private Rectangle viewport;
	private SpriteBatch batch;
	private World world;
	private RayHandler rayHandler;
	private Stage stage;
	private Skin skin;
	private Music music;
	
	private static TweenManager tweenManager;
	
	@Override
	public void create() {
		logger = new Logger(TAG, Globals.debugLevel);
		screens = new ObjectMap<String, LudumScreen>();
		nextScreen = null;
		viewport = new Rectangle(0.0f, 0.0f, 0.0f, 0.0f);
		camera = new OrthographicCamera(Globals.virtualWidth * Globals.pixelsToMetres,
										Globals.virtualHeight * Globals.pixelsToMetres);
		
		uiCamera = new OrthographicCamera(Globals.virtualWidth, Globals.virtualHeight);
		uiCamera.position.x = Globals.virtualWidth * 0.5f;
		uiCamera.position.y = Globals.virtualHeight * 0.5f;
		
		tweenManager = new TweenManager();
		Tween.registerAccessor(OrthographicCamera.class, new CameraTweener());
		Tween.registerAccessor(Light.class, new LightTweener());
		Tween.registerAccessor(RayHandler.class, new RayHandlerTweener());
		Tween.registerAccessor(Actor.class, new ActorTweener());
		Tween.registerAccessor(Vector2.class, new VectorTweener());
		Tween.setCombinedAttributesLimit(4);
		
		Assets.instance.init();
		
		world = new World(Globals.gravity, Globals.doSleep);
		
		rayHandler = new RayHandler(world);
		rayHandler.setAmbientLight(Globals.ambientLight);
		rayHandler.setShadows(true);
		
		batch = new SpriteBatch();
		
		skin = Assets.instance.get("data/ui/uiskin.json", Skin.class);
		stage = new Stage(Globals.virtualWidth, Globals.virtualHeight, true);
		
		screens.put("gameScreen", new GameScreen(this));
		screens.put("menuScreen", new MenuScreen(this));
		setScreen("menuScreen");
		
		music = Assets.instance.get("data/audio/song.ogg", Music.class);
		music.setLooping(true);
		music.play();
	}

	@Override
	public void dispose() {
		music.stop();
		Assets.instance.dispose();
		world.dispose();
		rayHandler.dispose();
		stage.dispose();
		skin.dispose();
		batch.dispose();
	}

	@Override
	public void render() {		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		
		Gdx.gl.glViewport((int) viewport.x,
						  (int) viewport.y,
						  (int) viewport.width,
						  (int) viewport.height);
		
		camera.update();
		uiCamera.update();
		super.render();
		tweenManager.update(Gdx.graphics.getDeltaTime());
		performScreenChange();
	}

	@Override
	public void resize(int width, int height) {
		// Calculate new aspect ratio
        float aspectRatio = (float)width / (float)height;
        
        float scale = 1.0f;
        
        // Calculate the scale we need to apply and the possible crop
        if(aspectRatio > Globals.aspectRatio)
        {
            scale = (float)height / (float)Globals.virtualHeight;
            viewport.x = (width - Globals.virtualWidth * scale) * 0.5f;
        }
        else if(aspectRatio < Globals.aspectRatio)
        {
            scale = (float)width / (float)Globals.virtualWidth;
            viewport.y = (height - Globals.virtualHeight * scale) * 0.5f;
        }
        else
        {
            scale = (float)width/(float)Globals.virtualWidth;
        }
        
        // New witdh and  height
        viewport.width = (float)Globals.virtualWidth * scale;
        viewport.height = (float)Globals.virtualHeight * scale;
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	public void setScreen(String name) {
		LudumScreen screen = screens.get(name);
		
		if (screen != null) {
			nextScreen = screen;
		}
		else {
			logger.error("invalid screen " + name);
		}
	}
	
	public OrthographicCamera getCamera() {
		return camera;
	}
	
	public OrthographicCamera getUICamera() {
		return uiCamera;
	}
	
	public World getWorld() {
		return world;
	}
	
	public RayHandler getRayHandler() {
		return rayHandler;
	}
	
	public Skin getSkin() {
		return skin;
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public SpriteBatch getBatch() {
		return batch;
	}
	
	public static TweenManager getTweenManager() {
		return tweenManager;
	}
	
	private void performScreenChange() {
		if (nextScreen != null) {
			logger.info("switching to screen " + screens.findKey(nextScreen, false));
			setScreen(nextScreen);
			Gdx.input.setInputProcessor(nextScreen);
			nextScreen = null;
		}
	}
}
