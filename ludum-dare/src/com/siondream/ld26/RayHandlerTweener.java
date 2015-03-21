package com.siondream.ld26;

import com.lights.box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;

import aurelienribon.tweenengine.TweenAccessor;

public class RayHandlerTweener implements TweenAccessor<RayHandler>{

	public static final int AmbientLight = 1;
//	public static final int Distance = 2;
//	public static final int Color = 3;
	
	@Override
	public int getValues(RayHandler handler, int tweenType, float[] returnValues) {
		switch(tweenType) {
		case AmbientLight:
			Color color = handler.getAmbientLight();
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
	public void setValues(RayHandler handler, int tweenType, float[] newValues) {
		switch(tweenType) {
		case AmbientLight:
			handler.setAmbientLight(newValues[0], newValues[1], newValues[2], newValues[3]);
			break;
		default:
			break;
		}
	}

}
