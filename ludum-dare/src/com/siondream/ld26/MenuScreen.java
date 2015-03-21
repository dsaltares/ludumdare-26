package com.siondream.ld26;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.lights.box2dLight.ConeLight;
import com.lights.box2dLight.PointLight;
import com.lights.box2dLight.RayHandler;

public class MenuScreen extends LudumScreen {

	public enum State {
		Initial,
		Defeat,
		Complete,
	}
	
	private ConeLight light;
	private PointLight lightRadius;
	private Vector2 lightPosition;
	private float lightDirection;
	private Vector2 currentDirection;
	private float orbitRadius;
	private float currentAngle;
	private TextureRegion backgroundRegion;
	private TextureRegion playerRegion;
	private Label ludumLabel;
	private Label startLabel;
	private Label messageLabel;
	private TiledMap map;
	private TmxMapLoader mapLoader;
	private MapBodyManager mapBodyManager;
	private OrthogonalTiledMapRenderer mapRenderer;
	private boolean initFinished;
	private boolean canStart;
	private Sound startSFX;
	
	public static State state = State.Initial; 
	
	public MenuScreen(LudumDare game) {
		super(game);
		
		mapBodyManager = new MapBodyManager(game.getWorld(),
											Globals.pixelsToMetres,
											"data/levels/materials.xml",
											Globals.debugLevel);
		
		mapLoader = new TmxMapLoader();
		
		startSFX = Assets.instance.get("data/audio/start.wav", Sound.class);
	}
	
	@Override
	public void show() {		
		backgroundRegion = new TextureRegion(Assets.instance.get("data/background.png", Texture.class));
		playerRegion = new TextureRegion(Assets.instance.get("data/player.png", Texture.class));

		map = mapLoader.load("data/menu.tmx");
		mapBodyManager.createPhysics(map, Globals.physicsLayer);
		mapRenderer = new OrthogonalTiledMapRenderer(map, Globals.pixelsToMetres);
	    
		OrthographicCamera camera = game.getCamera();
		camera.position.x = Globals.virtualWidth * 0.5f * Globals.pixelsToMetres;
		camera.position.y = Globals.virtualHeight * 0.5f * Globals.pixelsToMetres;
		
		Skin skin = game.getSkin();
		Stage stage = game.getStage();
		ludumLabel = new Label("Ludum Dare #26    -    David Saltares    -    http://siondream.com", skin);
		startLabel = new Label("Press any key to start", skin);
		
		switch (state) {
		case Initial:
			messageLabel = new Label("Can you escape the lightbyrinth?", skin);
			break;
		case Defeat:
			messageLabel = new Label("You didn't make it, fancy another go?", skin);
			break;
		case Complete:
			messageLabel = new Label("You did it! Thanks for playing", skin);
			break;
		}

		ludumLabel.pack();
		startLabel.pack();
		messageLabel.pack();
		stage.addActor(ludumLabel);
		stage.addActor(startLabel);
		stage.addActor(messageLabel);
		
		ludumLabel.setY(-100.0f);
		startLabel.setY(-100.0f);
		messageLabel.setY(-100.0f);
		
		lightPosition = new Vector2(0.0f, 0.0f);
		
		RayHandler rayHandler = game.getRayHandler();
		
		light = new ConeLight(rayHandler,
				  Globals.playerLightRays,
				  Globals.playerLightColor,
				  Globals.playerLightDistance,
				  lightPosition.x,
				  lightPosition.y,
				  0.0f,
				  Globals.playerLightAngle);


		light.setSoftnessLenght(Globals.playerLightSoftLength);
		light.setActive(false);
		
		lightRadius = new PointLight(rayHandler,
									 Globals.playerLightRays,
									 Globals.playerLightRadiusColor,
									 Globals.playerLightRadiusDistance,
									 lightPosition.x,
									 lightPosition.y);
		
		lightRadius.setActive(false);
		
	    triggerInAnimations();
	}
	
	@Override
	public void hide() {
		light.remove();
		lightRadius.remove();
		game.getStage().clear();
		map.dispose();
		mapBodyManager.destroyPhysics();
	}
	
	@Override
	public void render(float delta) {
		// UPDATE
		
		// Physics
		World world = game.getWorld();
		world.step(Gdx.graphics.getDeltaTime(), Globals.velocityIterations, Globals.positionIterations);
		world.clearForces();
		
		// UI
		game.getStage().act(delta);
		
		// Lights
		if (initFinished) {
			updateLight(delta);
		}
		else {
			lightPosition.set(lightRadius.getPosition());
			light.setDirection(lightDirection);
		}
		
		// RENDER
		OrthographicCamera camera = game.getCamera();
		
		SpriteBatch batch = game.getBatch();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(backgroundRegion,
				   0.0f,
				   0.0f,
				   0.0f,
				   0.0f,
				   backgroundRegion.getRegionWidth(),
				   backgroundRegion.getRegionHeight(),
				   Globals.pixelsToMetres,
				   Globals.pixelsToMetres,
				   0.0f);
		
		float width = playerRegion.getRegionWidth();
		float height = playerRegion.getRegionHeight();
		float originX = width * 0.5f;
		float originY = height * 0.5f;
		float scale = Globals.pixelsToMetres;
		float rotation = 0.0f;
		
		batch.draw(playerRegion,
				   lightPosition.x - originX,
				   lightPosition.y - originY,
				   originX,
				   originY,
				   width,
				   height,
				   scale,
				   scale,
				   rotation * MathUtils.radiansToDegrees);
		
		batch.end();
		
		mapRenderer.setView(camera);
		mapRenderer.render();
		
		RayHandler rayHandler = game.getRayHandler();
		rayHandler.setCombinedMatrix(camera.combined);
		rayHandler.updateAndRender();
		
		Stage stage = game.getStage();
		OrthographicCamera uiCamera = game.getUICamera();
		stage.setCamera(uiCamera);
		stage.draw();
	}
	
	private void updateLight(float delta) {
		currentAngle = (currentAngle + Globals.menuOrbitTime / MathUtils.PI2 * delta) % MathUtils.PI2;
		currentDirection.x = MathUtils.sin(currentAngle);
		currentDirection.y = MathUtils.cos(currentAngle);
		currentDirection.nor();
		currentDirection.scl(orbitRadius);
		lightPosition.set(Globals.menuOrbitPosition);
		lightPosition.add(currentDirection);
		
		light.setPosition(lightPosition);
		lightRadius.setPosition(lightPosition);
		
		currentDirection.set(Globals.menuOrbitPosition);
		currentDirection.sub(lightPosition);
		light.setDirection(MathUtils.atan2(currentDirection.y, currentDirection.x) * MathUtils.radiansToDegrees);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (canStart) {
			startSFX.play();
			triggerOutAnimations();
		}
		return false;
	}
	
	private void triggerInAnimations() {
		initFinished = false;
		canStart = false;
		lightDirection = 90.0f;//MathUtils.PI * 0.5f;
		Timeline timeline = Timeline.createSequence();
		
		
		timeline.beginSequence()
		
					.push(Tween.set(light, LightTweener.Position)
							   .target(Globals.virtualWidth * Globals.pixelsToMetres * 0.5f, -1.0f))
					.push(Tween.set(light, LightTweener.Color)
							   .target(0.0f, 0.0f, 0.0f, 0.0f))
					.push(Tween.set(lightRadius, LightTweener.Position)
							   .target(Globals.virtualWidth * Globals.pixelsToMetres * 0.5f, -1.0f))
					.push(Tween.set(lightRadius, LightTweener.Color)
							   .target(0.0f, 0.0f, 0.0f, 0.0f))
					.push(Tween.set(ludumLabel, ActorTweener.Position)
							   .target(20.0f, -30.0f, 0.0f))
					.push(Tween.set(startLabel, ActorTweener.Position)
							   .target((Globals.virtualWidth - startLabel.getWidth()) * 0.5f, -30.0f, 0.0f))
					.push(Tween.set(messageLabel, ActorTweener.Position)
							   .target(Globals.virtualWidth + 10.0f, 100.0f, 0.0f))
					.push(Tween.to(game.getRayHandler(), RayHandlerTweener.AmbientLight, Globals.menuFadeInTime)
							   .target(Globals.menuAmbientLightColor.r, Globals.menuAmbientLightColor.g, Globals.menuAmbientLightColor.b, Globals.menuAmbientLightColor.a)
							   .ease(TweenEquations.easeInQuad))
					
					.beginParallel()
						.push(Tween.to(ludumLabel, ActorTweener.Position, Globals.menuFadeInTime)
									.target(20.0f, 20.0f, 0.0f)
									.ease(TweenEquations.easeOutQuad))
						.push(Tween.to(light, LightTweener.Color, Globals.menuFadeInTime * 1.5f)
									.target(Globals.playerLightRadiusColor.r, Globals.playerLightRadiusColor.g, Globals.playerLightRadiusColor.b, Globals.playerLightRadiusColor.a)
									.ease(TweenEquations.easeInQuad))
						.push(Tween.to(lightRadius, LightTweener.Color, Globals.menuFadeInTime * 1.5f)
									.target(Globals.playerLightColor.r, Globals.playerLightColor.g, Globals.playerLightColor.b, Globals.playerLightColor.a)
									.ease(TweenEquations.easeInQuad))
					.end()
					
					.setCallback(new TweenCallback() {
						@Override
						public void onEvent(int type, BaseTween<?> source) {
							if (type == TweenCallback.COMPLETE)
								triggerPostInitAnimations();
						}
					})
					
				.end()
				.start(LudumDare.getTweenManager());
	}
	
	private void triggerPostInitAnimations() {
		canStart = true;
		
		light.setActive(true);
		lightRadius.setActive(true);
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence()
					.push(Tween.to(startLabel, ActorTweener.Position, Globals.menuFadeInTime * 1.5f)
							   .target((Globals.virtualWidth - startLabel.getWidth()) * 0.5f, 100.0f, 0.0f)
							   .ease(TweenEquations.easeOutQuad)
							   .setCallback(new TweenCallback() {
									@Override
									public void onEvent(int type, BaseTween<?> source) {
										if (type == TweenCallback.COMPLETE)
											triggerStartAnimations();
									}
								}))
					.push(Tween.to(messageLabel, ActorTweener.Position, Globals.menuFadeInTime * 1.5f)
							   .target(Globals.virtualWidth - messageLabel.getWidth() - 40.0f, 100.0f, 0.0f)
							   .ease(TweenEquations.easeOutQuad))
					.beginParallel()
						.push(Tween.to(light, LightTweener.Position, Globals.menuFadeInTime * 1.5f)
								   .target(Globals.virtualWidth * Globals.pixelsToMetres * 0.5f, 250.0f * Globals.pixelsToMetres, 0.0f)
								   .ease(TweenEquations.easeInQuad))
						.push(Tween.to(lightRadius, LightTweener.Position, Globals.menuFadeInTime * 1.5f)
								   .target(Globals.virtualWidth * Globals.pixelsToMetres * 0.5f, 250.0f * Globals.pixelsToMetres, 0.0f)
								   .ease(TweenEquations.easeInQuad))
						.setCallback(new TweenCallback() {
							@Override
							public void onEvent(int type, BaseTween<?> source) {
								if (type == TweenCallback.COMPLETE)
									initFinished();
							}
						})
					.end()
				.end()
				.start(LudumDare.getTweenManager());
	}
	
	private void initFinished() {
		initFinished = true;
		
		orbitRadius = lightPosition.dst(Globals.menuOrbitPosition);
		currentAngle = MathUtils.PI;
		currentDirection = new Vector2();
	}
	
	private void triggerStartAnimations() {
		Tween.to(startLabel, ActorTweener.Color, Globals.menuFadeInTime * 0.5f)
		   .target(1.0f, 1.0f, 1.0f, 0.0f)
		   .ease(TweenEquations.easeInQuad)
		   .repeatYoyo(Tween.INFINITY, 0.0f)
		   .start(LudumDare.getTweenManager());
	}
	
	private void triggerOutAnimations() {
		
		Tween.to(game.getRayHandler(), RayHandlerTweener.AmbientLight, Globals.menuFadeInTime)
		   	 .target(0.0f, 0.0f, 0.0f)
		   	 .ease(TweenEquations.easeInQuad)
		   	 .setCallback(new TweenCallback() {
				@Override
				public void onEvent(int type, BaseTween<?> source) {
					if (type == TweenCallback.COMPLETE) {
						game.setScreen("gameScreen");
					}
				}
		   	 })
		   	 .start(LudumDare.getTweenManager());
		
		Tween.to(light, LightTweener.Color, Globals.menuFadeInTime)
		   	 .target(0.0f, 0.0f, 0.0f)
		   	 .ease(TweenEquations.easeInQuad)		   	 
		   	 .start(LudumDare.getTweenManager());
		
		Tween.to(lightRadius, LightTweener.Color, Globals.menuFadeInTime)
		   	 .target(0.0f, 0.0f, 0.0f)
		   	 .ease(TweenEquations.easeInQuad)		   	 
		   	 .start(LudumDare.getTweenManager());
	}
	
}
