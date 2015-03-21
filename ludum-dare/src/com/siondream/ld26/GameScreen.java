package com.siondream.ld26;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import com.siondream.ld26.WorldController.GameState;

public class GameScreen extends LudumScreen {
	private int currentLevelIndex;
	private WorldController controller;
	private WorldRenderer renderer;
	
	private Array<String> levelList;
	
	GameScreen(LudumDare game) {
		super(game);
		
		controller = new WorldController(game);
		renderer = new WorldRenderer();
		
		currentLevelIndex = 0;
		levelList = new Array<String>();
		levelList.add(new String("data/levels/level01.tmx"));
		levelList.add(new String("data/levels/level02.tmx"));
		levelList.add(new String("data/levels/level03.tmx"));
		levelList.add(new String("data/levels/level04.tmx"));
		levelList.add(new String("data/levels/level05.tmx"));
	}
	
	@Override
	public void render(float delta) {
		controller.update(delta);
		renderer.render(controller);
		
		// Check if the level is finished
		GameState gameState = controller.getState();
		int nextLevel = -1;
		
		if (gameState == GameState.VictoryFinish) {
			nextLevel = currentLevelIndex + 1;
		}
		else if (gameState == GameState.DefeatFinish) {
			MenuScreen.state = MenuScreen.State.Defeat;
			currentLevelIndex = 0;
			game.setScreen("menuScreen");
		}
		
		if (nextLevel != -1) {
			if (nextLevel < levelList.size) {
				// Load next level
				currentLevelIndex = nextLevel;
				hide();
				show();
			}
			else {
				MenuScreen.state = MenuScreen.State.Complete;
				game.setScreen("menuScreen");
			}
		}
	}
	
	@Override
	public void show() {
		controller.init(levelList.get(currentLevelIndex));
		renderer.init(controller);
	}

	@Override
	public void hide() {
		controller.dispose();
		renderer.dispose();
	}	
	
	@Override
	public void dispose() {
		controller.dispose();
		renderer.dispose();
	}
	
	@Override
	public boolean scrolled(int amount) {
		OrthographicCamera camera = game.getCamera();
		camera.zoom += amount * 0.5f;
		return false;
	}
}
