package com.siondream.ld26;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CameraController {
	Entity target;
	OrthographicCamera camera;
	Rectangle bounds;
	Vector2 cameraTarget;
	Tween tween;
	
	public CameraController(OrthographicCamera camera, Entity target) {
		this.camera = camera;
		this.target = target;
		this.bounds = new Rectangle(0.0f, 0.0f, 0.0f, 0.0f);
		this.cameraTarget = new Vector2(0.0f, 0.0f);
	}
	
	public void init() {
		camera.position.x = target.getX();
		camera.position.y = target.getY();
	}
	
	public void setTarget(Entity target) {
		this.target = target;
	}
	
	public void setBounds(float x, float y, float width, float height) {
		bounds.x = x;
		bounds.x = y;
		bounds.width = width;
		bounds.height = height;
	}
	
	public void update(float delta) {
		if (target != null) {
			// Is the target is out of the bounds
			Vector2 targetPos = target.getPosition();
			float distanceSq = camera.position.dst2(targetPos.x, targetPos.y, 0.0f);
			
			if (distanceSq > Globals.cameraMaxDistanceFromTarget *  Globals.cameraMaxDistanceFromTarget) {
				// Set new target
				cameraTarget.x = targetPos.x;
				cameraTarget.y = targetPos.y;
				
				// Clamp target position according to
				if (hasBounds()) {
					cameraTarget.x = Math.max(cameraTarget.x, bounds.x + camera.viewportWidth * 0.5f);
					cameraTarget.y = Math.max(cameraTarget.y, bounds.y + camera.viewportHeight * 0.5f);
					cameraTarget.x = Math.min(cameraTarget.x, bounds.x - camera.viewportWidth * 0.5f);
					cameraTarget.y = Math.min(cameraTarget.y, bounds.y - camera.viewportHeight * 0.5f);
				}
				
				// Fire tweening
				tween = Tween.to(camera, CameraTweener.Position, Globals.cameraTweenTime)
							 .ease(TweenEquations.easeOutQuad)
							 .target(cameraTarget.x, cameraTarget.y, 0.0f)
							 .start();
			}
		}
		
		if (tween != null) {
			tween.update(delta);
		}
	}
	
	public boolean hasBounds() {
		return bounds.width > 0.0f && bounds.height > 0.0f;
	}
}
