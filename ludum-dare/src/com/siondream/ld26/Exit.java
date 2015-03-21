package com.siondream.ld26;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.lights.box2dLight.Light;
import com.lights.box2dLight.PointLight;

public class Exit extends Entity {

	private PointLight light;
	private Body body;
	
	public Exit(WorldController controller) {
		super(controller);
		init();
	}

	@Override
	public void init() {
		super.init();
		
		WorldController controller = getController();
		
		light = new PointLight(controller.getRayHandler(),
							   Globals.exitLightRays,
							   Globals.exitLightColor,
							   Globals.exitLightDistance,
							   getX(),
							   getY());
		
		
		CircleShape shape = new CircleShape();
		shape.setRadius(Globals.exitLightDistance * 0.15f);
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.fixedRotation = true;
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.isSensor = true;
		fixtureDef.shape = shape;
		
		body = controller.getWorld().createBody(bodyDef);
		body.createFixture(fixtureDef);
		body.setUserData(this);
		shape.dispose();
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
		light.setPosition(x, y);
		body.setTransform(getPosition(), getRotation());
	}
	
	public Light getLight() {
		return light;
	}
}
