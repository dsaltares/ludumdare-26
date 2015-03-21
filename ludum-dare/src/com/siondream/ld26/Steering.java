package com.siondream.ld26;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Steering {
	
	public static class SteeringData {
		public Vector2 linear = new Vector2(0.0f, 0.0f);
		public float angular;
		
		public void reset() {
			linear.x = linear.y = angular = 0.0f;
		}
		
		public void add(SteeringData other, float weight) {
			linear.add(other.linear.x * weight, other.linear.y * weight);
			angular += other.angular * weight;
		}
		
		public void add(SteeringData other) {
			add(other, 1.0f);
		}
	}
	
	public static abstract class SteeringBehavior {
		protected SteeringData steering;
		protected Enemy enemy;
		
		public SteeringBehavior(Enemy enemy) {
			this.steering = new SteeringData();
			this.enemy = enemy;
		}
		
		public abstract SteeringData update(float delta);
	}
	
	public static class Seek extends SteeringBehavior {

		protected Vector2 target;
		protected float maxAcceleration;
		
		public Seek(Enemy enemy, float maxAcceleration) {
			super(enemy);
			target = new Vector2(0.0f, 0.0f);
			this.maxAcceleration = maxAcceleration;
		}
		
		public void setTarget(Vector2 target) {
			this.target.x = target.x;
			this.target.y = target.y;
		}

		@Override
		public SteeringData update(float delta) {
			steering.reset();
			
			// Get movement direction towards target
			Vector2 position = enemy.getPosition();
			steering.linear.set(target.x, target.y);
			steering.linear.sub(position.x, position.y);
			
			// Normalize
			steering.linear.nor();
			
			// Full acceleration
			steering.linear.scl(maxAcceleration);
			
			return steering;
		}
	}
	
	public static class Flee extends SteeringBehavior {

		protected Vector2 target;
		protected float maxAcceleration;
		
		public Flee(Enemy enemy, float maxAcceleration) {
			super(enemy);
			target = new Vector2(0.0f, 0.0f);
			this.maxAcceleration = maxAcceleration;
		}
		
		public void setTarget(Vector2 target) {
			this.target.x = target.x;
			this.target.y = target.y;
		}

		@Override
		public SteeringData update(float delta) {
			steering.reset();
			
			// Get movement direction from target
			Vector2 position = enemy.getPosition();
			steering.linear.set(position.x, position.y);
			steering.linear.sub(target.x, target.y);
			
			// Normalize
			steering.linear.nor();
			
			// Full acceleration
			steering.linear.scl(maxAcceleration);
			
			return steering;
		}
	}
	
	public static class FollowPath extends Seek {
		
		float arriveDistance;
		float predictTime;
		float pathAngleThreshold;
		Vector2[] path;
		int currentPathIndex;
		Vector2 futurePos;
		protected Vector2 toPointDirection;
		protected Vector2 pathDirection;
		boolean done;
		
		public FollowPath(Enemy enemy,
						  float maxAcceleration,
						  float arriveDistance,
						  float predictTime,
						  float pathAngleThreshold) {
			
			super(enemy, maxAcceleration);
			this.arriveDistance = arriveDistance;
			this.predictTime = predictTime;
			this.currentPathIndex = 0;
			this.pathAngleThreshold = pathAngleThreshold;
			this.futurePos = new Vector2();
			this.toPointDirection = new Vector2();
			this.pathDirection = new Vector2();
			this.done = false;
		}
		
		public Vector2 getCurrentPoint() {
			return path != null? path[currentPathIndex] : null;
		}
		
		public boolean isDone() {
			return done;
		}
		
		public void setPath(Vector2[] path) {
			this.path = path;
			this.currentPathIndex = 0;
			this.done = false;
		}
		
		@Override
		public SteeringData update(float delta) {
			steering.reset();
			
			if (path != null) {
				Vector2 position = enemy.getPosition();
				Vector2 velocity = enemy.getBody().getLinearVelocity();
				
				// Predict pos
				futurePos.x = position.x + velocity.x * predictTime;
				futurePos.y = position.y + velocity.y * predictTime;
				
				// Find next index in path
				currentPathIndex = getNextPathIndex(futurePos);
				
				// Set target for seek
				super.setTarget(path[currentPathIndex]);
				
				// Delegate in seek
				return super.update(delta);
			}
			
			return steering;
		}
		
		private int getNextPathIndex(Vector2 futurePos) {
			if (currentPathIndex == path.length - 1) {
				this.done = true;
				return currentPathIndex;
			}
			
			if (futurePos.dst2(path[currentPathIndex]) < arriveDistance * arriveDistance) {
				return currentPathIndex + 1;
			}
			
			toPointDirection.set(path[currentPathIndex]);
			toPointDirection.sub(futurePos);
			toPointDirection.nor();
			
			pathDirection.set(path[currentPathIndex + 1]);
			pathDirection.sub(path[currentPathIndex]);
			pathDirection.nor();
			
			if (toPointDirection.dot(pathDirection) > pathAngleThreshold) {
				return currentPathIndex + 1;
			}
			
			return currentPathIndex;
		}
	}
	
	public static class Separation extends SteeringBehavior {
		
		private float checkDistance;
		private float decayCoefficient;
		private float maxAcceleration;
		private Vector2 direction;
		
		public Separation(Enemy enemy, float checkDistance, float decayCoefficient, float maxAcceleration) {
			super(enemy);
			this.checkDistance = checkDistance;
			this.decayCoefficient = decayCoefficient;
			this.maxAcceleration = maxAcceleration;
			this.direction = new Vector2();
		}

		@Override
		public SteeringData update(float delta) {
			steering.reset();
			
			Vector2 position = enemy.getPosition();
			
			Array<Enemy> enemies = enemy.getController().getEnemies();
			for (int i = 0; i < enemies.size; ++i) {
				Enemy other = enemies.get(i);
				if (other != enemy) {
					Vector2 otherPosition = other.getPosition();
					direction.set(position);
					direction.sub(otherPosition);
					float distanceSq = direction.len2();
					
					if (distanceSq < checkDistance * checkDistance) {
						float strength = Math.min(decayCoefficient / distanceSq,
												  maxAcceleration);
						
						direction.nor();
						direction.scl(strength);
						steering.linear.add(direction);
					}
				}
			}
			
			steering.linear.nor();
			
			return steering;
		}
		
	}
}
