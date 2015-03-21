package com.siondream.ld26;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.lights.box2dLight.RayHandler;

public class WorldRenderer implements Disposable {
	private OrthogonalTiledMapRenderer mapRenderer;
	private ShapeRenderer shapeRenderer;
	
	public WorldRenderer() {
		
	}
	
	public void init(WorldController controller) {
		mapRenderer = new OrthogonalTiledMapRenderer(controller.getMap(), Globals.pixelsToMetres);
		shapeRenderer = new ShapeRenderer();
//		box2DRenderer = new Box2DDebugRenderer(Globals.drawBodies,
//											   Globals.drawJoints,
//											   Globals.drawABBs,
//											   Globals.drawInactiveBodies,
//											   Globals.drawVelocities,
//											   Globals.drawContacts);

	}
	
	@Override
	public void dispose() {
		mapRenderer.dispose();
		shapeRenderer.dispose();
//		box2DRenderer.dispose();
	}
	
	public void render(WorldController controller) {
		OrthographicCamera camera = controller.getCamera();
		
		mapRenderer.setView(camera);
		mapRenderer.render();
		
		SpriteBatch batch = controller.getBatch();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		controller.getPlayer().render(batch);
		
		for (Enemy enemy : controller.getEnemies()) {
			enemy.render(batch);
		}
		
		for (Bullet bullet : controller.getBullets()) {
			bullet.render(batch);
		}
		
		batch.end();
		
		RayHandler rayHandler = controller.getRayHandler();
		rayHandler.setCombinedMatrix(camera.combined);
		rayHandler.updateAndRender();
		
		Stage stage = controller.getStage();
		OrthographicCamera uiCamera = controller.getUICamera();
		stage.setCamera(uiCamera);
		stage.draw();
		
		debugDraw(controller);
	}
	
	private void debugDraw(WorldController controller) {
		
		if (Globals.debug ) {
			OrthographicCamera camera = controller.getCamera();
			shapeRenderer.setProjectionMatrix(camera.combined);
			
			if (Globals.drawGrid) {
				// Debug shapes
				shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.line(-Globals.virtualWidth * 0.5f, 0.0f, Globals.virtualWidth * 0.5f, 0.0f);
				shapeRenderer.line(0.0f, -Globals.virtualHeight * 0.5f, 0.0f, Globals.virtualHeight * 0.5f);
				
				shapeRenderer.setColor(0.0f, 1.0f, 0.0f, 1.0f);
				
				for (int i = -100; i <= 100; ++i) {
					if (i == 0)
						continue;
					
					shapeRenderer.line(-Globals.virtualWidth * 0.5f, i, Globals.virtualWidth * 0.5f, i);
				}
				
				for (int i = -100; i <= 100; ++i) {
					if (i == 0)
						continue;
					
					shapeRenderer.line(i, -Globals.virtualHeight * 0.5f, i, Globals.virtualHeight * 0.5f);
				}
				
				shapeRenderer.end();
			}
			
			if (Globals.drawTiles){
				
				shapeRenderer.begin(ShapeType.Filled);
				
				TiledMapTileLayer layer = (TiledMapTileLayer)controller.getMap().getLayers().get("background");
				
				for (int x = 0; x < layer.getWidth(); ++x) {
					for (int y = 0; y < layer.getHeight(); ++y) {
						boolean walkable = Boolean.parseBoolean(layer.getCell(x, y).getTile().getProperties().get("walkable", "false", String.class));
						
						if (walkable) {
							shapeRenderer.setColor(0.0f, 1.0f, 0.0f, 0.1f);
						}
						else {
							shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 0.1f);
						}
						shapeRenderer.rect(x * layer.getTileWidth() * Globals.pixelsToMetres,
										   y * layer.getTileHeight() * Globals.pixelsToMetres,
										   layer.getTileWidth() * Globals.pixelsToMetres,
										   layer.getTileHeight() * Globals.pixelsToMetres);
					}
				}
				shapeRenderer.end();
			}
			
			if (Globals.drawPaths) {
				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(1.0f, 0.0f, 1.0f, 1.0f);
				for (Enemy enemy : controller.getEnemies()) {
					Vector2[] path = enemy.getPath();
					
					if (path != null) {
						for (int i = 0; i < path.length - 1; ++i) {
							shapeRenderer.line(path[i].x, path[i].y, path[i + 1].x, path[i + 1].y);
						}
					}
				}
				shapeRenderer.end();
				
				shapeRenderer.begin(ShapeType.Filled);
				for (Enemy enemy : controller.getEnemies()) {
					Vector2 point = enemy.getFollowPath().getCurrentPoint();
					
					
					if (point != null) {
						shapeRenderer.circle(point.x, point.y, 0.2f);
					}
				}
				
				shapeRenderer.end();
			}
			
//			box2DRenderer.render(controller.getWorld(), camera.combined);
		}
	}
}
