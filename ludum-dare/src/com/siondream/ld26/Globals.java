package com.siondream.ld26;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Logger;

public class Globals {
	// Application config
	public static final float virtualWidth = 1280.0f;
	public static final float virtualHeight = 720.0f;
	public static final float aspectRatio = virtualWidth / virtualHeight;
	public static final float metresToPixels = 64.0f;
	public static final float pixelsToMetres = 1.0f / metresToPixels;
	
	// Player
	public static final float playerHealth = 50.0f;
	public static final float playerRecoverTime = 2.0f;
	public static final float playerMana = 100.0f;
	public static final float playerMaxSpeed = 5.0f;
	public static final float playerForce = 2.5f;
	public static final float playerFriction = 0.8f;
	public static final float playerDensity = 0.1f;
	public static final float playerRestitution = 0.0f;
	public static final float playerRadius = 0.25f;
	public static final float playerLightDistance = 20.0f;
	public static final float playerLightRadiusDistance = 8.0f;
	public static final float playerLightAngle = 40.0f;
	public static final float playerLightSoftLength = 5.0f;
	public static final int playerLightRays = 10;
	public static final Color playerLightColor = new Color(0.7f, 0.7f, 1.0f, 0.9f);
	public static final Color playerLightRadiusColor = new Color(0.7f, 0.7f, 1.0f, 0.5f);
	public static final float playerShootDelay = 2.0f;
	public static final Color playerLightColorDamage = new Color(1.0f, 0.7f, 0.7f, 0.9f);
	public static final Color playerLightRadiusColorDamage = new Color(1.0f, 0.7f, 0.7f, 0.9f);
	public static final float playerAdvantageTime = 2.0f;
	public static final Color playerAdvantageLight = new Color(1.0f, 1.0f, 1.0f, 0.05f);
	
	// Enemies
	public static final float enemyFriction = 0.2f;
	public static final float enemyDensity = 0.1f;
	public static final float enemyRestitution = 0.2f;
	public static final float enemyRadius = 0.25f;
	public static final float enemyMaxSpeed = 3.5f;
	public static final float enemyForce = 2.0f;
	public static final float enemyRePathDistance = 5.0f;
	public static final float enemyEndDistance = 0.1f;
	public static final float enemyPredictTime = 0.05f;
	public static final float enemyPathAngleThreshold = 0.8f;
	public static final float enemySeparationCheckDistance = 5.00f;
	public static final float enemySeparationDecayCoefficient = 0.05f;
	public static final float enemyFollowPathWeight = 0.6f;
	public static final float enemySeparationWeight = 0.4f;
	public static final float enemySeekWeight = 0.6f;
	public static final float enemyDamage = 25.0f;
	
	// Exit portal
	public static final int exitLightRays = 10;
	public static final float exitLightDistance = 10.0f;
	public static final Color exitLightColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
	
	// Bullet 
	public static final int bulletLightRays = 5;
	public static final float bulletLightDistance = 1.0f;
	public static final Color bulletLightColor = new Color(1.0f, 0.0f, 1.0f, 1.0f);
	public static final float bulletSpeed = 5.0f;
	
	// Physics
	public static final Vector2 gravity = new Vector2(0.0f, 0.0f);
	public static final boolean doSleep = false;
	public static final int velocityIterations = 6;
	public static final int positionIterations = 10;
	
	// Level data
	public static final String physicsLayer = "collisions";
	public static final String playerSpawn = "player.spawn";
	public static final String enemySpawn = "enemy.spawn";
	public static final String mazeExit = "maze.exit";
	public static final String eventsLayer = "events";
	public static final String backgroundLayer = "background";
	public static final String tileset = "test";
	public static final String maxEnemiesProperty = "maxEnemies";
	public static final Color ambientLight = new Color(1.0f, 1.0f, 1.0f, 0.01f);
	
	// Camera
	public static final float cameraMaxDistanceFromTarget = 3.0f;
	public static final float cameraTweenTime = 1.0f;
	
	// Spawner
	public static final float spawnerMinTime = 5.0f;
	public static final float spawnerMinDistance = 6.0f;
	public static final int spawnerMaxEnemies = 10;
	
	// Pathfinder
	public static final long pathFinderBudgetMs = 5;
	
	// Game state control
	public static final float stateVictoryTime = 1.0f;
	public static final float stateVictoryDistance = Globals.exitLightDistance * 2.0f;
	public static final float stateDefeatTime = 1.5f;
	public static final Color stateDefeatColor = new Color(1.0f, 1.0f, 1.0f, 0.00f);
	public static final Color stateDefeatPlayerColor = new Color(0.0f, 0.0f, 0.0f, 0.00f);
	public static final Color stateDefeatPlayerRadiusColor = new Color(0.0f, 0.0f, 0.0f, 0.00f);
	
	// HUD
	public static final float hudBarWidth = 300.0f;
	public static final float hudBarTweenTime = 0.5f;
	public static final Color hudSwarmColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
	
	// Tips
	public static final float tipsFirstTime = 0.5f;
	public static final float tipsNextTime = 0.5f;
	public static final float tipsTransitionTime = 0.5f;
	public static final float tipsShowingTimePerCharacter = 0.07f;
	public static final float tipsMinShowingTime = 2.5f;
	
	// Menu
	public static final float menuFadeInTime = 1.0f;
	public static final Color menuAmbientLightColor = new Color(1.0f, 1.0f, 1.0f, 0.1f);
	public static final float menuOrbitTime = 5.0f;
	public static final Vector2 menuOrbitPosition = new Vector2(Globals.virtualWidth * 0.5f * Globals.pixelsToMetres,
																600.0f * Globals.pixelsToMetres);
	
	// Debug
	public static final boolean debug = false;
	public static final int debugLevel = Logger.NONE;
	public static final boolean drawGrid = false;
	public static final boolean drawTiles = false;
	public static final boolean drawPaths = false;
	public static final boolean drawBodies = true;
	public static final boolean drawJoints = false;
	public static final boolean drawABBs = false;
	public static final boolean drawInactiveBodies = false;
	public static final boolean drawVelocities = false;
	public static final boolean drawContacts = false;
	public static final boolean drawStage = true;
	public static final float scrollSpeed = 5.0f;
	
}