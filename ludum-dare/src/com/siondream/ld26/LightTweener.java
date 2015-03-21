package com.siondream.ld26;

import com.badlogic.gdx.math.Vector2;
import com.lights.box2dLight.Light;
import com.badlogic.gdx.graphics.Color;

import aurelienribon.tweenengine.TweenAccessor;

public class LightTweener implements TweenAccessor<Light>{

	public static final int Position = 1;
	public static final int Distance = 2;
	public static final int Color = 3;
	
	@Override
	public int getValues(Light light, int tweenType, float[] returnValues) {
		switch(tweenType) {
		case Position:
			Vector2 position = light.getPosition();
			returnValues[0] = position.x;
			returnValues[1] = position.y;
			return 2;
		case Distance:
			returnValues[0] = light.getDistance();
			return 1;
		case Color:
			Color color = light.getColor();
			returnValues[0] = color.r;
			returnValues[1] = color.g;
			returnValues[2] = color.b;
			returnValues[3] = color.a;
			return 4;
		default:
			return 0;
		}
	}

	@Override
	public void setValues(Light light, int tweenType, float[] newValues) {
		switch(tweenType) {
		case Position:
			light.setPosition(newValues[0], newValues[1]);
			break;
		case Distance:
			light.setDistance(newValues[0]);
			break;
		case Color:
			light.setColor(newValues[0], newValues[1], newValues[2], newValues[3]);
			break;
		default:
			break;
		}
	}

}
