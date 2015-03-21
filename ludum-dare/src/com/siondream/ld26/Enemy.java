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
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.siondream.ld26.PathFinder.ResultCode;
import com.siondream.ld26.Steering.FollowPath;
import com.siondream.ld26.Steering.Seek;
import com.siondream.ld26.Steering.Separation;
import com.siondream.ld26.Steering.SteeringBehavior;
import com.siondream.ld26.Steering.SteeringData;

public class Enemy extends Entity implements PathFinder.Callback {
	
	private TextureRegion region;
	private Body body;
	private Vector2[] path;
	private SteeringData steering;
	private Array<SteeringBehavior> steeringBehaviors;
	private Array<Float> steeringWeights;
	private float damage;

	public Enemy(WorldController controller) {
		super(controller);

		region = new TextureRegion(Assets.instance.get("data/enemy.png", Texture.class));
		
		steering = new SteeringData();
		steeringBehaviors = new Array<SteeringBehavior>();
		steeringWeights = new Array<Float>();
		
		FollowPath followPath = new FollowPath(this,
											   Globals.enemyForce,
											   Globals.enemyEndDistance,
											   Globals.enemyPredictTime,
											   Globals.enemyPathAngleThreshold);
		
		Separation separation = new Separation(this,
											   Globals.enemySeparationCheckDistance,
											   Globals.enemySeparationDecayCoefficient,
											   Globals.enemyForce);
		
		Seek seek = new Seek(this, Globals.enemyForce);
		
		steeringBehaviors.add(followPath);
		steeringWeights.add(Globals.enemyFollowPathWeight);
		
		steeringBehaviors.add(separation);
		steeringWeights.add(Globals.enemySeparationWeight);
		
		steeringBehaviors.add(seek);
		steeringWeights.add(0.0f);
		
		init();
	}
	
	@Override
	public void init() {
		super.init();
		
		WorldController controller = getController();
		
		CircleShape shape = new CircleShape();
		shape.setRadius(Globals.enemyRadius);
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(getPosition());
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.fixedRotation = true;
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = Globals.enemyFriction;
		fixtureDef.density = Globals.enemyDensity;
		fixtureDef.restitution = Globals.enemyRestitution;
		fixtureDef.shape = shape;
		
		World world = controller.getWorld();
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);
		body.setUserData(this);
		shape.dispose();
		
		damage = Globals.enemyDamage;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		if (body != null) {
			getController().getWorld().destroyBody(body);
			body = null;
		}
	}
	
	public Vector2[] getPath() {
		return path;
	}
	
	public Body getBody() {
		return body;
	}
	
	public float getDamage() {
		return damage;
	}
	
	public FollowPath getFollowPath() {
		return (FollowPath)steeringBehaviors.get(0);
	}
	
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		body.setTransform(getPosition(), body.getTransform().getRotation());
	}
	
	@Override
	public void setPosition(Vector2 position) {
		setPosition(position.x, position.y);
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
		setRotation(bodyRot);
		
		updateAI(delta);
		
		steering.reset();
		
		Seek seek = (Seek)steeringBehaviors.get(2);
		seek.setTarget(getController().getPlayer().getPosition());
		
		for (int i = 0; i < steeringBehaviors.size; ++i) {
			steering.add(steeringBehaviors.get(i).update(delta), steeringWeights.get(i));
		}
		
		steering.linear.limit(Globals.enemyForce);
		
		Vector2 velocity = body.getLinearVelocity();
		
		if (canMove() && steering.linear.len2() > 0.0f ) {
			body.applyForceToCenter(steering.linear.x, steering.linear.y, true);
			velocity.limit(Globals.enemyMaxSpeed);
		
			// Look where you're going
			setRotation(MathUtils.atan2(velocity.y, velocity.x));
		}
		else {
			velocity.x = 0.0f;
			velocity.y = 0.0f;
			body.setAngularVelocity(0.0f);
		}
		
		body.setLinearVelocity(velocity);
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
	
	private boolean canMove() {
		State state = getState();
		
		return (state != State.Die && state != State.Erase);
	}
	
	private void updateAI(float delta) {
		switch(getState()) {
		case Idle:
			updateIdle();
			break;
		case Move:
			updateMove();
			break;
		default:
			break;
		}
	}
	
	private void updateIdle() {
		WorldController controller = getController();
		Vector2 playerPos = controller.getPlayer().getPosition();
		Vector2 position = getPosition();
		controller.getPathFinder().requestPath(position.x, position.y, playerPos.x, playerPos.y, this);
		setState(State.WaitingForPath);
	}
	
	private void updateMove() {
		WorldController controller = getController();
		Vector2 playerPos = controller.getPlayer().getPosition();
		Vector2 position = getPosition();
		
		// Check distance with the player (may need to repath)
		if (path[path.length - 1].dst2(playerPos) > Globals.enemyRePathDistance * Globals.enemyRePathDistance) {
			setState(State.Idle);
			return;
		}
		
		if (getFollowPath().isDone()) {
			steeringWeights.set(0, 0.0f);
			steeringWeights.set(2, Globals.enemySeekWeight);
		}
	}

	@Override
	public void onPathRequest(long id, ResultCode code, Array<Vector2> path) {
		if (code != PathFinder.ResultCode.PathFound) {
			return;
		}
		
		this.path = new Vector2[path.size];
		
		for (int i = 0; i < path.size; ++i) {
			Vector2 wayPoint = path.get(i);
			this.path[i] = new Vector2(wayPoint.x, wayPoint.y);
		}
		
		getFollowPath().setPath(this.path);
		steeringWeights.set(0, Globals.enemyFollowPathWeight);
		steeringWeights.set(2, 0.0f);
		
		setState(State.Move);
	}
}
