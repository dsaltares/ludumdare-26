package com.siondream.ld26;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.lights.box2dLight.PointLight;

public class Bullet extends Entity implements Poolable {
	private PointLight light;
	private Body body;
	private Vector2 direction;
	private TextureRegion region;
	
	public Bullet(WorldController controller) {
		super(controller);
	}
	
	@Override
	public void init() {
		super.init();
		
		WorldController controller = getController();
		
		region = new TextureRegion(Assets.instance.get("data/enemy.png", Texture.class));
		
		light = new PointLight(controller.getRayHandler(),
							   Globals.bulletLightRays,
							   Globals.bulletLightColor,
							   Globals.bulletLightDistance,
							   getX(),
							   getY());
		
		light.setXray(true);
		
		
		CircleShape shape = new CircleShape();
		shape.setRadius(Globals.bulletLightDistance * 0.25f);
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.fixedRotation = true;
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = Globals.playerFriction;
		fixtureDef.density = Globals.playerDensity;
		fixtureDef.restitution = Globals.playerRestitution;
		fixtureDef.shape = shape;
		
		body = controller.getWorld().createBody(bodyDef);
		body.createFixture(fixtureDef);
		body.setUserData(this);
		shape.dispose();
		
		direction = new Vector2();
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
	}
	
	@Override
	public void setPosition(Vector2 position) {
		setPosition(position.x, position.y);
	}
	
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		body.setTransform(getPosition(), body.getTransform().getRotation());
		light.setPosition(x, y);
	}

	@Override
	public void reset() {
		light.setActive(false);
		body.setActive(false);
		setPosition(0.0f, 0.0f);
		direction.set(0.0f, 0.0f);
	}
	
	public PointLight getLight() {
		return light;
	}
	
	public Body getBody() {
		return body;
	}
	
	public void setDirection(float x, float y) {
		direction.set(x, y);
		direction.nor();
	}
	
	@Override
	public void update(float delta) {
		Vector2 velocity = body.getLinearVelocity();
	
		body.applyForceToCenter(direction.x * Globals.bulletSpeed,
								direction.y * Globals.bulletSpeed,
								true);
		velocity.limit(Globals.bulletSpeed);
		body.setLinearVelocity(velocity);
		
		Vector2 bodyPos = body.getTransform().getPosition();
		light.setPosition(bodyPos);
		setPosition(bodyPos);
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
