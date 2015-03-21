package com.siondream.ld26;

import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;

public class Assets implements Disposable, AssetErrorListener {

	public static final Assets instance = new Assets();
	
	private static final String TAG = "Assets";
	private Logger logger;
	private AssetManager manager;
	
	private Assets() {
		logger = new Logger(TAG, Globals.debugLevel);
		manager = new AssetManager();
		manager.setErrorListener(this);
	}
	
	public void init() {
		manager.load("data/player.png", Texture.class);
		manager.load("data/enemy.png", Texture.class);
		manager.load("data/bullet.png", Texture.class);
		manager.load("data/ui/uiskin.json", Skin.class);
		manager.load("data/ui/bar.png", Texture.class);
		manager.load("data/background.png", Texture.class);
		manager.load("data/audio/hit.wav", Sound.class);
		manager.load("data/audio/killEnemy.wav", Sound.class);
		manager.load("data/audio/shoot.wav", Sound.class);
		manager.load("data/audio/start.wav", Sound.class);
		manager.load("data/audio/victory.wav", Sound.class);
		manager.load("data/audio/advantage.wav", Sound.class);
		manager.load("data/audio/song.ogg", Music.class);
		
		manager.finishLoading();
		
		manager.get("data/ui/bar.png", Texture.class).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}
	
	public synchronized <T> T get (String fileName) {
		return manager.get(fileName);
	}
	
	public synchronized <T> T get (String fileName, Class<T> type) {
		return manager.get(fileName, type);
	}

	@Override
	public void dispose() {
		manager.dispose();
	}
	
	@Override
	public void error(String fileName, Class type, Throwable throwable) {
		logger.error("error loading " + fileName);
	}
}


public class MyGame extends Game {

	private AssetManager manager;
	
	@Override
	public void create() {
		super.create()
		
		manager = new AssetManager();
		loadAssets();
	}
	
	@Override
	public void dispose() {
		manager.dispose();
	}
	
	public AssetManager getAssetManager() {
		return manager;
	}
	
	private void loadAssets() {
		manager.load("data/player.png", Texture.class);
		manager.load("data/enemy.png", Texture.class);
		manager.load("data/bullet.png", Texture.class);
		manager.load("data/ui/uiskin.json", Skin.class);
		manager.load("data/ui/bar.png", Texture.class);
		manager.load("data/background.png", Texture.class);
		manager.load("data/audio/hit.wav", Sound.class);
		manager.load("data/audio/killEnemy.wav", Sound.class);
		manager.load("data/audio/shoot.wav", Sound.class);
		manager.load("data/audio/start.wav", Sound.class);
		manager.load("data/audio/victory.wav", Sound.class);
		manager.load("data/audio/advantage.wav", Sound.class);
		manager.load("data/audio/song.ogg", Music.class);
		
		manager.finishLoading();
	}
}