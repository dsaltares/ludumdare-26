package com.siondream.ld26;


import java.util.Iterator;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.lights.box2dLight.Light;
import com.lights.box2dLight.RayHandler;
import com.siondream.ld26.Entity.State;

public class WorldController implements Disposable, ContactListener {
	
	public enum GameState {
		Start,
		Playing,
		Victory,
		Defeat,
		VictoryFinish,
		DefeatFinish,
	}
	
	private LudumDare game;
	private TiledMap map;
	private TmxMapLoader mapLoader;
	private PathFinder pathFinder;
	private MapBodyManager mapBodyManager;
	private Player player;
	private Exit exit;
	private Array<Enemy> enemies;
	private EnemySpawnManager spawnManager;
	private Array<Bullet> bullets;
	private BulletPool bulletPool;
	private CameraController cameraController;
	private TipManager tipManager;
	private ObjectMap<Class, ObjectMap<Class, ContactListener>> contactListeners;
	private ObjectMap<Class, ContactListener> contactGeometryListeners;
	private GameState state;
	private Sound hitSFX;
	private Sound victorySFX;
	private Sound killEnemySFX;
	private Sound shootSFX;
	private Sound advantageSFX;
	
	// UI
	private Image healthBar;
	private Image energyBar;
	private Image swarmBar;
	private float lastHealth;
	private float lastEnergy;
	private float lastSwarm;
	
	public WorldController(LudumDare game) {
		this.game = game;

		
		
		mapBodyManager = new MapBodyManager(getWorld(),
											Globals.pixelsToMetres,
											"data/levels/materials.xml",
											Globals.debugLevel);
		
		getWorld().setContactListener(this);
		
		
		mapLoader = new TmxMapLoader();
		enemies = new Array<Enemy>();
		bullets = new Array<Bullet>();
		bulletPool = new BulletPool(this);
		cameraController = new CameraController(game.getCamera(), player);
		pathFinder = new PathFinder();
		spawnManager = new EnemySpawnManager(this);
		tipManager = new TipManager(this);
		
		contactListeners = new ObjectMap<Class, ObjectMap<Class, ContactListener>>();
		setContactListener(Player.class, Enemy.class, new PlayerEnemyCollisionHandler());
		setContactListener(Player.class, Exit.class, new PlayerExitCollisionHandler());
		setContactListener(Enemy.class, Bullet.class, new EnemyBulletCollisionHandler());
		
		contactGeometryListeners = new ObjectMap<Class, ContactListener>();
		setContactGeometryListener(Bullet.class, new BulletGeometryCollisionHandler());
		
		hitSFX = Assets.instance.get("data/audio/hit.wav", Sound.class);
		killEnemySFX = Assets.instance.get("data/audio/killEnemy.wav", Sound.class);
		victorySFX = Assets.instance.get("data/audio/victory.wav", Sound.class);
		shootSFX = Assets.instance.get("data/audio/shoot.wav", Sound.class);
		advantageSFX = Assets.instance.get("data/audio/advantage.wav", Sound.class);
	}
	
	public void init(String levelName) {
		player = new Player(this);
		exit = new Exit(this);
		loadLevel(levelName);
		pathFinder.init(map);
		levelSetup();
		cameraController.setTarget(player);
		cameraController.init();
		tipManager.init();
		getRayHandler().setAmbientLight(Globals.ambientLight);
		createUI();
		start();
	}
	
	@Override
	public void dispose() {
		map.dispose();
		mapBodyManager.destroyPhysics();
		player.dispose();
		exit.dispose();
		spawnManager.dispose();
		tipManager.dispose();
		
		for (Bullet bullet : bullets) {
			bullet.dispose();
		}
		
		bullets.clear();
		
		for (Enemy enemy : enemies) {
			enemy.dispose();
		}
		
		enemies.clear();
		getStage().clear();
	}
	
	public OrthographicCamera getCamera() {
		return game.getCamera();
	}
	
	public OrthographicCamera getUICamera() {
		return game.getUICamera();
	}
	
	public Stage getStage() {
		return game.getStage();
	}
	
	public Skin getSkin() {
		return game.getSkin();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public TiledMap getMap() {
		return map;
	}
	
	public World getWorld() {
		return game.getWorld();
	}
	
	public SpriteBatch getBatch() {
		return game.getBatch();
	}
	
	public RayHandler getRayHandler() {
		return game.getRayHandler();
	}
	
	public PathFinder getPathFinder() {
		return pathFinder;
	}
	
	public Array<Enemy> getEnemies() {
		return enemies;
	}
	
	public Array<Bullet> getBullets() {
		return bullets;
	}
	
	public GameState getState() {
		return state;
	}
	
	public void shoot(Vector2 position, Vector2 direction) {
		shootSFX.play();
		Bullet bullet = bulletPool.obtain();
		bullet.setPosition(position);
		bullet.setDirection(direction.x, direction.y);
		bullets.add(bullet);
	}
	
	public void update(float delta) {
		if (getState() == GameState.Playing) {			
			// Update all elements
			player.update(delta);
			
			for (int i = 0; i < enemies.size; ++i) {
				enemies.get(i).update(delta);
			}
			
			for (int i = 0; i < bullets.size; ++i) {
				bullets.get(i).update(delta);
			}
			
			// Spawner
			spawnManager.update(delta);
			
			// Camera
			cameraController.update(delta);
			
			// Pathfinder
			pathFinder.update();
			
			// Tips
			tipManager.update(delta);
			
			// Physics
			World world = getWorld();
			world.step(Gdx.graphics.getDeltaTime(), Globals.velocityIterations, Globals.positionIterations);
			world.clearForces();
			
			// UI
			getStage().act(delta);
			
			// Remove deleted entities
			removeDeletedEntities();
		}
		
		updateUI();
	}
	
	
	private void loadLevel(String levelName) {
		map = mapLoader.load(levelName);
		mapBodyManager.createPhysics(map, Globals.physicsLayer);
	}
	
	private void levelSetup() {
		MapObjects eventObjects = map.getLayers().get(Globals.eventsLayer).getObjects();
		
		RectangleMapObject playerSpawn = (RectangleMapObject)eventObjects.get(Globals.playerSpawn);
		Rectangle playerSpawnRect = playerSpawn.getRectangle();
		player.init();
		player.setPosition(playerSpawnRect.getX() * Globals.pixelsToMetres,
						   playerSpawnRect.getY() * Globals.pixelsToMetres);
		
		Ellipse exitEllipse = ((EllipseMapObject)(eventObjects.get(Globals.mazeExit))).getEllipse();
		exit.init();
		exit.setPosition(exitEllipse.x * Globals.pixelsToMetres,
						 exitEllipse.y * Globals.pixelsToMetres);
		
		// Enemies spawnPoints
		spawnManager.dispose();
		for (int i = 0; i < eventObjects.getCount(); ++i) {
			MapObject mapObject = eventObjects.get(i);
			
			if (mapObject.getName().equals(Globals.enemySpawn)) {
				Rectangle enemySpawn = ((RectangleMapObject)mapObject).getRectangle();
				spawnManager.addSpawnPoint(enemySpawn.getX() * Globals.pixelsToMetres,
										   enemySpawn.getY() * Globals.pixelsToMetres);
			}
		}
			
		spawnManager.setMaxEnemies(Integer.parseInt(map.getProperties().get(Globals.maxEnemiesProperty,
																			"" + Globals.spawnerMaxEnemies,
																			String.class)));
		
		// Tips
		Array<String> tipKeys = new Array<String>();
		MapProperties properties = map.getProperties();
		Iterator<String> it = properties.getKeys();
		
		while(it.hasNext()) {
			String name = it.next();
			
			if (name.startsWith("tip.")) {
				tipKeys.add(name);
				
			}
		}
		
		tipKeys.sort();
		
		for (String key : tipKeys) {
			tipManager.addTip(properties.get(key, "", String.class));	
		}
	}
	
	private void createUI() {
		healthBar = new Image(Assets.instance.get("data/ui/bar.png", Texture.class));
		energyBar = new Image(Assets.instance.get("data/ui/bar.png", Texture.class));
		swarmBar = new Image(Assets.instance.get("data/ui/bar.png", Texture.class));
		
		swarmBar.setColor(Globals.hudSwarmColor);
		
		lastHealth = Globals.playerHealth * 0.1f / 100.0f;
		lastSwarm = 0.0f;
		lastEnergy = 0.1f;
		
		Table table = new Table(getSkin());
		table.setWidth(360.0f);
		table.row();
		table.add("Health").align(Align.right).padRight(10.0f);
		table.add(healthBar).width(Globals.hudBarWidth).fillX();
		table.row();
		table.add("Energy").align(Align.right).padRight(10.0f);
		table.add(energyBar).width(Globals.hudBarWidth).fillX();
		table.row();
		table.add("Swarm").align(Align.right).padRight(10.0f);
		table.add(swarmBar).width(Globals.hudBarWidth).fillX();
		
		getStage().addActor(table);
		table.pack();
		
		table.setX(20.0f);
		table.setY(Globals.virtualHeight - table.getHeight() - 20.0f);
		
		swarmBar.setScaleX(lastSwarm * Globals.hudBarWidth / swarmBar.getWidth());
		
		updateUI();
	}
	
	private void updateUI() {
		float newHealth = player.getHealth() / Globals.playerHealth;
		float newEnergy = Math.min(player.getTimeSinceLastShot() / Globals.playerShootDelay, 1.0f);
		float newSwarm = spawnManager.getSwarmLevel();
		
		if (newHealth != lastHealth) {
			Tween.to(healthBar, ActorTweener.Scale, Globals.hudBarTweenTime)
				 .target(newHealth * Globals.hudBarWidth / healthBar.getWidth(), 1.0f)
				 .ease(TweenEquations.easeInQuad)
				 .start(LudumDare.getTweenManager());
			
		}

		energyBar.setScaleX(lastEnergy * Globals.hudBarWidth / energyBar.getWidth());
		
		if (newEnergy == 1.0f) {
			energyBar.setColor(Globals.bulletLightColor.r, Globals.bulletLightColor.g, Globals.bulletLightColor.b, 1.0f);
		}
		else {
			energyBar.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
		
		lastHealth = newHealth;
		lastEnergy = newEnergy;
		
		if (newSwarm != lastSwarm) {
			Tween.to(swarmBar, ActorTweener.Scale, Globals.hudBarTweenTime)
				 .target(newSwarm * Globals.hudBarWidth / healthBar.getWidth(), 1.0f)
				 .ease(TweenEquations.easeInQuad)
				 .start(LudumDare.getTweenManager());
			lastSwarm = newSwarm;
		}
	}
	
	private void removeDeletedEntities() {
		for (int i = 0; i < enemies.size;) {
			Enemy enemy = enemies.get(i);
			
			if (enemy.getState() == State.Erase) {
				enemy.dispose();
				enemies.removeIndex(i);
			}
			else {
				++i;
			}
		}
		
		for (int i = 0; i < bullets.size;) {
			Bullet bullet = bullets.get(i);
			
			if (bullet.getState() == State.Erase) {
				bullet.dispose();
				bullets.removeIndex(i);
			}
			else {
				++i;
			}
		}
	}

	private void setState(GameState state) {
		this.state = state;
	}
	
	@Override
	public void beginContact(Contact contact) {
		Object dataA = contact.getFixtureA().getBody().getUserData();
		Object dataB = contact.getFixtureB().getBody().getUserData();
		
		if (dataA != null && dataB != null) {
			ContactListener collisionHandler = getContactListener(dataA.getClass(), dataB.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.beginContact(contact);
			}
		}
		else if (dataA != null) {
			ContactListener collisionHandler = getContactGeometryListener(dataA.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.beginContact(contact);
			}
		}
		else if (dataB != null) {
			ContactListener collisionHandler = getContactGeometryListener(dataB.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.beginContact(contact);
			}
		}
	}

	@Override
	public void endContact(Contact contact) {
		Object dataA = contact.getFixtureA().getBody().getUserData();
		Object dataB = contact.getFixtureB().getBody().getUserData();
		
		if (dataA != null && dataB != null) {
			ContactListener collisionHandler = getContactListener(dataA.getClass(), dataB.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.endContact(contact);
			}
		}
		else if (dataA != null) {
			ContactListener collisionHandler = getContactGeometryListener(dataA.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.endContact(contact);
			}
		}
		else if (dataB != null) {
			ContactListener collisionHandler = getContactGeometryListener(dataB.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.endContact(contact);
			}
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		Object dataA = contact.getFixtureA().getBody().getUserData();
		Object dataB = contact.getFixtureB().getBody().getUserData();
		
		if (dataA != null && dataB != null) {
			ContactListener collisionHandler = getContactListener(dataA.getClass(), dataB.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.preSolve(contact, oldManifold);
			}
		}
		else if (dataA != null) {
			ContactListener collisionHandler = getContactGeometryListener(dataA.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.preSolve(contact, oldManifold);
			}
		}
		else if (dataB != null) {
			ContactListener collisionHandler = getContactGeometryListener(dataB.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.preSolve(contact, oldManifold);
			}
		}
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		Object dataA = contact.getFixtureA().getBody().getUserData();
		Object dataB = contact.getFixtureB().getBody().getUserData();
		
		if (dataA != null && dataB != null) {
			ContactListener collisionHandler = getContactListener(dataA.getClass(), dataB.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.postSolve(contact, impulse);
			}
		}
		else if (dataA != null) {
			ContactListener collisionHandler = getContactGeometryListener(dataA.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.postSolve(contact, impulse);
			}
		}
		else if (dataB != null) {
			ContactListener collisionHandler = getContactGeometryListener(dataB.getClass());
			
			if (collisionHandler != null) {
				collisionHandler.postSolve(contact, impulse);
			}
		}
	}
	
	public void setContactListener(Class c1, Class c2, ContactListener listener) {
		ObjectMap<Class, ContactListener> listeners;
		
		listeners = contactListeners.get(c1);
		
		if (listeners == null) {
			listeners = new ObjectMap<Class, ContactListener>();
			contactListeners.put(c1, listeners);
		}
		
		listeners.put(c2, listener);
		
		listeners = contactListeners.get(c2);
		
		if (listeners == null) {
			listeners = new ObjectMap<Class, ContactListener>();
			contactListeners.put(c2, listeners);
		}
		
		listeners.put(c1, listener);
	}
	
	public void setContactGeometryListener(Class c1, ContactListener listener) {
		contactGeometryListeners.put(c1, listener);
	}
	
	private ContactListener getContactListener(Class c1, Class c2) {
		ObjectMap<Class, ContactListener> listeners = contactListeners.get(c1);
		return listeners != null? listeners.get(c2) : null;
	}
	
	private ContactListener getContactGeometryListener(Class c1) {
		return contactGeometryListeners.get(c1);
	}
	
	
	private class ContactAdapter implements ContactListener {
		@Override
		public void beginContact(Contact contact) {}

		@Override
		public void endContact(Contact contact) {}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {}
	}
	
	private class PlayerEnemyCollisionHandler extends ContactAdapter {
		@Override
		public void beginContact(Contact contact) {
			Object dataA = contact.getFixtureA().getBody().getUserData();
			Object dataB = contact.getFixtureB().getBody().getUserData();
			Player player = null;
			Enemy enemy = null;
			
			if (dataA instanceof Player) {
				player = (Player)dataA;
				enemy = (Enemy)dataB;
			} 
			else {
				player = (Player)dataB;
				enemy = (Enemy)dataA;
			}
			
			if (player.getState() != State.Damage) {
				hitSFX.play();
				player.damage(enemy.getDamage());
				
				if (player.getHealth() <= 0.0f) {
					defeat();
				}
				else {
					advantageSFX.play();
					damagePlayer();
				}
			}
		}
	}
	
	private class PlayerExitCollisionHandler extends ContactAdapter {
		@Override
		public void beginContact(Contact contact) {
			victorySFX.play();
			victory();
		}
	}
	
	private class EnemyBulletCollisionHandler extends ContactAdapter {
		@Override
		public void beginContact(Contact contact) {
			Object dataA = contact.getFixtureA().getBody().getUserData();
			Object dataB = contact.getFixtureB().getBody().getUserData();
			Bullet bullet = null;
			Enemy enemy = null;
			
			if (dataA instanceof Bullet) {
				bullet = (Bullet)dataA;
				enemy = (Enemy)dataB;
			}
			else {
				bullet = (Bullet)dataB;
				enemy = (Enemy)dataA;
			}
			
			bullet.setState(State.Erase);
			enemy.setState(State.Erase);
			killEnemySFX.play();
		}
	}
	
	private class BulletGeometryCollisionHandler extends ContactAdapter {
		@Override
		public void beginContact(Contact contact) {
			Object dataA = contact.getFixtureA().getBody().getUserData();
			Object dataB = contact.getFixtureB().getBody().getUserData();
			Bullet bullet = dataA != null && dataA instanceof Bullet? (Bullet)dataA : (Bullet)dataB;
			bullet.setState(State.Erase);
			
		}
	}
	
	private static class BulletPool extends Pool<Bullet> {

		private WorldController controller;
		
		public BulletPool(WorldController controller) {
			this.controller = controller;
		}
		
		@Override
		protected Bullet newObject() {
			return new Bullet(controller);
		}
		
		@Override
		public Bullet obtain() {
			Bullet bullet = super.obtain();
			bullet.getLight().setActive(true);
			//bullet.getBody().setActive(true);
			return bullet;
		}
	}
	
	private void start() {
		setState(GameState.Playing);
	}
	
	private void victory() {
		setState(GameState.Victory);
		
		// Victory SFX
		
		
		// Victory light tween
		TweenCallback callback = new TweenCallback() {
			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					setState(GameState.VictoryFinish);
				}
			}
		};
		
		
		Light light = exit.getLight();
		Timeline timeline = Timeline.createSequence();
		
		light.setXray(true);
		
		timeline.push(Tween.to(light, LightTweener.Distance, Globals.stateVictoryTime / 2.0f)
					 	   .target(Globals.stateVictoryDistance)
					 	   .ease(TweenEquations.easeInQuad)
					 	   .repeatYoyo(2, 0.2f))
				.setCallback(callback)
				.start(LudumDare.getTweenManager());
	}
	
	private void damagePlayer() {
		// Damage SFX
		
		
		// Increase lights radius fast
		// Maintain
		// Go back
		Timeline timeline = Timeline.createSequence();
		RayHandler rayHandler = getRayHandler();
		
		timeline.beginSequence()
					.push(Tween.to(rayHandler, RayHandlerTweener.AmbientLight, Globals.playerAdvantageTime * 0.15f)
							   .target(Globals.playerAdvantageLight.r, Globals.playerAdvantageLight.g, Globals.playerAdvantageLight.b, Globals.playerAdvantageLight.a)
							   .ease(TweenEquations.easeInQuad))
					.pushPause(Globals.playerAdvantageTime * 0.7f)
					.push(Tween.to(rayHandler, RayHandlerTweener.AmbientLight, Globals.playerAdvantageTime * 0.15f)
							   .target(Globals.ambientLight.r, Globals.ambientLight.g, Globals.ambientLight.b, Globals.ambientLight.a)
							   .ease(TweenEquations.easeInQuad))
				.end()
				.start(LudumDare.getTweenManager());
	}
	
	private void defeat() {
		setState(GameState.Defeat);
		
		// Defeat SFX
		
		
		// Defeat light tween
		TweenCallback callback = new TweenCallback() {
			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					setState(GameState.DefeatFinish);
				}
			}
		};
		
		
		Light light = player.getLight();
		Light lightRadius = player.getLightRadius();
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginParallel()
					.push(Tween.to(getRayHandler(), RayHandlerTweener.AmbientLight, Globals.stateDefeatTime * 0.75f)
						 	   .target(Globals.stateDefeatColor.r, Globals.stateDefeatColor.g, Globals.stateDefeatColor.b, Globals.stateDefeatColor.a)
						 	   .ease(TweenEquations.easeInQuad))
					.push(Tween.to(light, LightTweener.Color, Globals.stateDefeatTime * 0.75f)
							   .target(Globals.stateDefeatPlayerColor.r, Globals.stateDefeatPlayerColor.g, Globals.stateDefeatPlayerColor.b, Globals.stateDefeatPlayerColor.a)
							   .ease(TweenEquations.easeInQuad))
					.push(Tween.to(light, LightTweener.Distance, Globals.stateDefeatTime * 0.75f)
							   .target(0.0f)
							   .ease(TweenEquations.easeInQuad))
					.push(Tween.to(lightRadius, LightTweener.Distance, Globals.stateDefeatTime)
							   .target(0.0f)
							   .ease(TweenEquations.easeInQuad))
				.end()
				.setCallback(callback)
				.start(LudumDare.getTweenManager());
	}
	
}
