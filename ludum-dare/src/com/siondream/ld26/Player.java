package com.siondream.ld26;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.lights.box2dLight.ConeLight;
import com.lights.box2dLight.Light;
import com.lights.box2dLight.PointLight;

public class Player extends Entity {
	private float health;
	private float mana;
	private TextureRegion region;
	private Body body;
	private Vector2 desiredDirection;
	private Vector2 spawnPosition;
	private Light light;
	private Light lightRadius;
	private float timeSinceLastShot;
	private float timeSinceLastHit;

	public Player(WorldController controller) {
		super(controller);
		
		region = new TextureRegion(Assets.instance.get("data/player.png", Texture.class));
		desiredDirection = new Vector2(0.0f, 0.0f);
		spawnPosition = new Vector2(0.0f, 0.0f);
		
		init();
	}

	@Override
	public void init() {
		super.init();
		
		WorldController controller = getController();
		
		health = Globals.playerHealth;
		mana = Globals.playerMana;
		
		CircleShape shape = new CircleShape();
		shape.setRadius(Globals.playerRadius);
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.fixedRotation = true;
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = Globals.playerFriction;
		fixtureDef.density = Globals.playerDensity;
		fixtureDef.restitution = Globals.playerRestitution;
		fixtureDef.shape = shape;
		
		World world = controller.getWorld();
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);
		body.setUserData(this);
		shape.dispose();
		
		light = new ConeLight(controller.getRayHandler(),
							  Globals.playerLightRays,
							  Globals.playerLightColor,
							  Globals.playerLightDistance,
							  getX(),
							  getY(),
							  getRotation() * MathUtils.radiansToDegrees,
							  Globals.playerLightAngle);

		
		light.setSoftnessLenght(Globals.playerLightSoftLength);
	
		lightRadius = new PointLight(controller.getRayHandler(),
									 Globals.playerLightRays,
									 Globals.playerLightRadiusColor,
									 Globals.playerLightRadiusDistance,
									 getX(),
									 getY());
		
		timeSinceLastShot = Float.POSITIVE_INFINITY;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		if (body != null) {
			getController().getWorld().destroyBody(body);
			body = null;
		}
		
		if (light != null) {
			light.remove();
			light = null;
		}
		
		if (lightRadius != null) {
			lightRadius.remove();
			lightRadius = null;
		}
	}
	
	public float getHealth() {
		return health;
	}
	
	public float getMana() {
		return mana;
	}
	
	public float getTimeSinceLastShot() {
		return timeSinceLastShot;
	}
	
	public Light getLight() {
		return light;
	}
	
	public Light getLightRadius() {
		return lightRadius;
	}
	
	public void damage(float healthPoints) {
		if (getState() != State.Damage) {
			health = health - healthPoints;
			
			if (healthPoints > 0.0f) {
				setState(State.Damage);
				timeSinceLastHit = 0.0f;
				light.setColor(Globals.playerLightColorDamage);
				lightRadius.setColor(Globals.playerLightRadiusColorDamage);
			}
			
			if (health <= 0.0f) {
				setState(State.Die);
				health = 0.0f;
			}
		}
	}
	
	public void useMana(float manaPoints) {
		mana = Math.max(0.0f, mana - manaPoints);
	}
	
	@Override
	public void setPosition(Vector2 position) {
		setPosition(position.x, position.y);
	}
	
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		body.setTransform(getPosition(), body.getTransform().getRotation());
	}
	
	@Override
	public void setRotation(float rotation) {
		super.setRotation(rotation);
		body.setTransform(getPosition(), rotation);
	}
	
	@Override
	public void update(float delta) {
		Vector2 position = getPosition();
		Transform transform = body.getTransform();
		Vector2 bodyPos = transform.getPosition();
		float bodyRot = transform.getRotation();
		
		setPosition(bodyPos);
		setRotation(getRotation());
		
		desiredDirection.x = 0.0f;
		desiredDirection.y = 0.0f;
		
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			desiredDirection.x = 1.0f;
		}
		else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			desiredDirection.x = -1.0f;
		}
		
		if (Gdx.input.isKeyPressed(Keys.UP)) {
			desiredDirection.y = 1.0f;
		}
		else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
			desiredDirection.y = -1.0f;
		}
		
		desiredDirection.nor();
		Vector2 velocity = body.getLinearVelocity();
		
		if (desiredDirection.len2() > 0.0f ) {
			body.applyForceToCenter(desiredDirection.x * Globals.playerForce,
									desiredDirection.y * Globals.playerForce,
									true);
			velocity.limit(Globals.playerMaxSpeed);
			
			// Look where you're going
			setRotation(MathUtils.atan2(desiredDirection.y, desiredDirection.x));
		}
		else {
			velocity.x = 0.0f;
			velocity.y = 0.0f;
			body.setAngularVelocity(0.0f);
		}
		
		body.setLinearVelocity(velocity);
		
		// Update light pos
		light.setPosition(getPosition());
		light.setDirection(getRotation() * MathUtils.radiansToDegrees);
		lightRadius.setPosition(getPosition());
		
		// Shooting
		if (timeSinceLastShot > Globals.playerShootDelay) {
			if (Gdx.input.isKeyPressed(Keys.SPACE)) {
				float rotation = getRotation();
				desiredDirection.x = MathUtils.cos(rotation);
				desiredDirection.y = MathUtils.sin(rotation);
				desiredDirection.nor();
				spawnPosition.set(getPosition());
				spawnPosition.add(desiredDirection.x * Globals.playerRadius * 2f, desiredDirection.y * Globals.playerRadius * 2f);
				getController().shoot(spawnPosition, desiredDirection);
				timeSinceLastShot = 0.0f;
			}
		}
		else {
			timeSinceLastShot += delta;
		}
		
		if (getState() == State.Damage) {
			timeSinceLastHit += delta;
			
			if (timeSinceLastHit > Globals.playerRecoverTime) {
				setState(State.Idle);
				light.setColor(Globals.playerLightColor);
				lightRadius.setColor(Globals.playerLightRadiusColor);
			}
		}
	}
	
	@Override
	public void render(SpriteBatch batch) {
		float width = region.getRegionWidth();
		float height = region.getRegionHeight();
		float originX = width * 0.5f;
		float originY = height * 0.5f;
		float scale = getScale() * Globals.pixelsToMetres;
		
		batch.draw(region,
				   getX() - originX,
				   getY() - originY,
				   originX,
				   originY,
				   width,
				   height,
				   scale,
				   scale,
				   getRotation() * MathUtils.radiansToDegrees);
	}
}
