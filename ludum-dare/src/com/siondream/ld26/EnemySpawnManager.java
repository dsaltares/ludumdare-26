package com.siondream.ld26;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.lights.box2dLight.RayHandler;

public class EnemySpawnManager implements Disposable {
	
	private WorldController controller;
	private Array<SpawnPoint> spawnPoints;
	private double gameTime;
	private float timeSinceLastSpawn;
	private int maxEnemies;
	
	private class SpawnPoint {
		private Vector2 position;
		private float timeSinceLastSpawn;
		
		public SpawnPoint(float x, float y) {
			this.position = new Vector2(x, y);
			this.timeSinceLastSpawn = 0.0f;
		}
		
		public void spawn() {
			Enemy enemy = new Enemy(controller);
			enemy.setPosition(position);
			controller.getEnemies().add(enemy);
			timeSinceLastSpawn = 0.0f;
		}
		
		public void update(float delta) {
			timeSinceLastSpawn += delta;
		}
		
		public Vector2 getPosition() {
			return position;
		}
		
		public float getTimeSinceLastSpawn() {
			return timeSinceLastSpawn;
		}
	}
	
	public EnemySpawnManager(WorldController controller) {
		this.controller = controller;
		this.gameTime = 0.0;
		this.timeSinceLastSpawn = 0.0f;
		this.spawnPoints = new Array<SpawnPoint>();
		this.maxEnemies = Globals.spawnerMaxEnemies;
	}
	
	public void addSpawnPoint(float x, float y) {
		spawnPoints.add(new SpawnPoint(x, y));
	}
	
	public void setMaxEnemies(int maxEnemies) {
		this.maxEnemies = maxEnemies;
	}

	public float getSwarmLevel() {
		return (float)controller.getEnemies().size / (float)Globals.spawnerMaxEnemies;
	}
	
	@Override
	public void dispose() {
		spawnPoints.clear();
		gameTime = 0.0;
	}
	
	public void update(float delta) {
		// Update times
		gameTime += delta;
		timeSinceLastSpawn += delta;
		
		for (SpawnPoint spawnPoint : spawnPoints) {
			spawnPoint.update(delta);
		}
		
		// Run spawn policy
		if (!shouldSpawn()) {
			return;
		}
		
		// Spawn point picking logic
		SpawnPoint spawnPoint = pickBestSpawnPoint();
		
		if (spawnPoint == null) {
			return;
		}
		
		// Spawn
		spawnPoint.spawn();
		timeSinceLastSpawn = 0.0f;
	}
	
	private boolean shouldSpawn() {
		return timeSinceLastSpawn > Globals.spawnerMinTime && controller.getEnemies().size < maxEnemies; 
	}
	
	private SpawnPoint pickBestSpawnPoint() {
		RayHandler rayHandler = controller.getRayHandler();
		Vector2 playerPosition = controller.getPlayer().getPosition();
		SpawnPoint bestSpawnPoint = null;
		float minDistanceSq = Float.POSITIVE_INFINITY;
		
		for (SpawnPoint spawnPoint : spawnPoints) {
			Vector2 spawnPosition = spawnPoint.getPosition();
			float distanceSq = playerPosition.dst2(spawnPosition);
			
			if (!rayHandler.pointAtLight(spawnPosition.x, spawnPosition.y) &&
				distanceSq > Globals.spawnerMinDistance * Globals.spawnerMinDistance &&
				distanceSq < minDistanceSq) {
				bestSpawnPoint = spawnPoint;
				minDistanceSq = distanceSq;
			}
		}
		
		return bestSpawnPoint;
	}
}
