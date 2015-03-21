package com.siondream.ld26;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class TipManager implements Disposable {
	
	private WorldController controller;
	private Label tipLabel;
	private Array<String> tips;
	private int currentTipIndex;
	private float timeUntilNextTip;
	private boolean showingTip;
	
	public TipManager(WorldController controller) {
		this.controller = controller;
		this.tips = new Array<String>();
		
		this.tipLabel = new Label("", controller.getSkin());
		this.timeUntilNextTip = Globals.tipsFirstTime;
		this.currentTipIndex = 0;
		this.showingTip = false;
	}

	public void init() {
		controller.getStage().addActor(tipLabel);
	}
	
	@Override
	public void dispose() {
		tipLabel.remove();
		tips.clear();
		timeUntilNextTip = Globals.tipsFirstTime;
		currentTipIndex = 0;
		showingTip = false;
	}
	
	public void update(float delta) {
		if (!showingTip) {
			timeUntilNextTip -= delta;
		}
		
		if (!showingTip && timeUntilNextTip < 0.0f) {
			showNextTip();
		}
	}
	
	public void addTip(String string) {
		tips.add(string);
	}
	
	private void showNextTip() {
		if (currentTipIndex < tips.size) {
			// Set new text
			tipLabel.setText(tips.get(currentTipIndex));
			
			tipLabel.pack();
			
			TweenCallback callback = new TweenCallback() {
				@Override
				public void onEvent(int type, BaseTween<?> source) {
					if (type == TweenCallback.COMPLETE) {
						showingTip = false;
					}
				}
			};
			
			// Tweening
			Timeline timeline = Timeline.createSequence();
			timeline.beginSequence()
						.push(Tween.set(tipLabel, ActorTweener.Position)
						 	   .target(Globals.virtualWidth - tipLabel.getWidth() - 30.0f , Globals.virtualHeight, 0.0f))
						.push(Tween.to(tipLabel, ActorTweener.Position, Globals.tipsTransitionTime)
									.target(Globals.virtualWidth - tipLabel.getWidth() - 30.0f , Globals.virtualHeight - tipLabel.getHeight() - 20.0f , 0.0f)
							 	 	.ease(TweenEquations.easeInQuad))
						.pushPause(Math.max(Globals.tipsShowingTimePerCharacter * tips.get(currentTipIndex).length(), Globals.tipsMinShowingTime))
						.push(Tween.to(tipLabel, ActorTweener.Position, Globals.tipsTransitionTime)
								   .target(Globals.virtualWidth - tipLabel.getWidth() - 30.0f , Globals.virtualHeight, 0.0f)
							 	   .ease(TweenEquations.easeInQuad))
					.end()
					.setCallback(callback)
					.start(LudumDare.getTweenManager());
			
			// Increase tip counter
			++currentTipIndex;
			
			showingTip = true;
			timeUntilNextTip = Globals.tipsNextTime;
		}
	}
}
