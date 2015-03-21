package com.siondream.ld26;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import aurelienribon.tweenengine.TweenAccessor;


public class VectorTweener implements TweenAccessor<Vector2>{

	public static final int Position = 1;
	
	@Override
	public int getValues(Vector2 vector, int tweenType, float[] returnValues) {
		switch(tweenType) {
		case Position:
			returnValues[0] = vector.x;
			returnValues[1] = vector.y;
			return 2;
		default:
			return 0;
		}
	}

	@Override
	public void setValues(Vector2 vector, int tweenType, float[] newValues) {
		switch(tweenType) {
		case Position:
			vector.x = newValues[0];
			vector.y = newValues[1];
			break;
		default:
			break;
		}
	}

}
