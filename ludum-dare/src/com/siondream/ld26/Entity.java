package com.siondream.ld26;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public abstract class Entity implements Disposable {
	
	public enum State {
		Idle,
		WaitingForPath,
		Move,
		Attack,
		Damage,
		Die,
		Erase,
	}
	
	
	private WorldController controller;
	private Vector2 position;
	private float rotation;
	private float scale;
	private State state;

	
	public Entity(WorldController controller) {
		this.controller = controller;
		position = new Vector2(0.0f, 0.0f);
		init();
	}
	
	public void init() {
		dispose();
		position.x = 0.0f;
		position.y = 0.0f;
		rotation = 0.0f;
		scale = 1.0f;
		state = State.Idle;
	}
	
	public float getX() {
		return position.x;
	}
	
	public float getY() {
		return position.y;
	}
	
	public Vector2 getPosition() {
		return position;
	}
	
	public float getRotation() {
		return rotation;
	}
	
	public float getScale() {
		return scale;
	}
	
	public State getState() {
		return state;
	}
	
	public WorldController getController() {
		return controller;
	}
	
	public void setPosition(float x, float y) {
		position.x = x;
		position.y = y;
	}
	
	public void setPosition(Vector2 position) {
		setPosition(position.x, position.y);
	}
	
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	
	public void setScale(float scale) {
		this.scale = scale; 
	}
	
	public void rotate(float angle) {
		rotation = (rotation + angle) % MathUtils.PI2;
	}
	
	public void scale(float factor) {
		scale *= factor;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public void update(float delta) {
		
	}
	
	public void render(SpriteBatch batch) {
		
	}

	@Override
	public void dispose() {
		
	}
}
