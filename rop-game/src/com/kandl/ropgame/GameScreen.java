package com.kandl.ropgame;

/*
 * HOW libgdx VIEWPORTS WORK, AND HOW THE SCENE DOES THEM:
 * Ok, this is so I can remember what the hell I was doing in the future.
 * The Viewport is the "size" of any particular part of the stage being rendered, in terms of the stage.
 * 		- ie. if the viewport is 1x1, then the things created in the stage get a percentage of the final screen = size*100.
 * What the camera then does is it scales the whole thing to match the device.
 * So, how does this impact my UI? Welp, glad you asked. See, what I -WANT- to happen is this:
 * 		- If 1024x600 (native), then all's well, no scaling/stretching needed.
 * 		- If 1280x800 (10.1"), then the right column should scale, and the left column should scale THEN stretch.
 * 			- Only one actual IMAGE will have to stretch, and it'll be defined as .9, all good; the other is just in a location in a cell,
 * 			  scale automagically and be reflowed.
 * Where's the problem? Don't want the right column to stretch, but I want the left column to. The problem, then, is this; how do I
 * define the viewport such that it takes up the full screen, BUT only one column fucks up when that happens. In essence, tell the UI "world"
 * to shrink when I upscale. (narrow).
 * 
 * Possible solution: Viewport goes to either 1280x800 or 1024x600, depending on aspect ratio of resize; 1024/600 > 1.7, so, should be safe
 * to use 1.7 as the delimiter for 1.6 or not. What should happen, then, is the UI should tell it's table to reflow itself, using viewport
 * size (ie. table resize, validate(), I think), and all to-do is replace right column width with 280, instead of 210. Left is still fill,
 * so when reflow happens, it'll stretch to take up space. Similarly, bottom row...Oh. Well then...Ah, fuck it, just run with it for now.
 */

/*
 * Drag and Drop, in scenes.scene2d.util. Will love them, use them for the UI recipes, for making the sandwich...
 * Really nothing I can't do. Incredibly nice. 
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Scaling;
import com.kandl.ropgame.managers.GroupManager;
import com.kandl.ropgame.ui.UILayer;

public class GameScreen implements Screen{
	public static final int FRONT = 0;
	public static final int INGREDIENTS = 1;
	public static final int GRILL = 2;
	public static final int ASSEMBLY = 3;
	
	private final UILayer UILayer;
	private final Stage[] Scene = new Stage[4];
	private Stage ActiveScene;
	private Image frontBackground;
	
	public GameScreen() {
		//Create UI, input processors
		UILayer = new UILayer(1280, 800, true);
		
		//Create scenes
		Scene[0] = createFrontScreen();
		Scene[1] = createMakeScreen();
		Scene[2] = createGrillScreen();
		Scene[3] = createCutScreen();
		ActiveScene = Scene[0];
		
		InputMultiplexer input = new InputMultiplexer();
		input.addProcessor(UILayer);
		input.addProcessor(ActiveScene);
		Gdx.input.setInputProcessor(input);
	}

	private Stage createCutScreen() {
		Stage scene = new Stage(1280, 800, true, UILayer.getSpriteBatch());
		Texture background = RopGame.assets.get("img/backgrounds/cuttingBoard.png", Texture.class);
		scene.addActor(new Image(new TextureRegionDrawable(new TextureRegion(background, 0, 224, 1280, 800))));
		for (Actor a: scene.getActors()) {
			a.setPosition(-50, 0);
		}
		return scene;
	}
	
	private Stage createGrillScreen() {
		Stage scene = new Stage(1280, 800, true, UILayer.getSpriteBatch());
		return scene;
	}

	private Stage createMakeScreen() {
		Stage scene = new Stage(1280, 800, true, UILayer.getSpriteBatch());
		return scene;
	}

	private Stage createFrontScreen() {
		Stage scene = new Stage(1280, 800, true, UILayer.getSpriteBatch());
		
		// Set listener that moves front world
		scene.addListener(new DragListener() {
			@Override
			public void drag (InputEvent event, float x, float y, int pointer) {
				float dx = -getDeltaX();
				if (Scene[0].getRoot().getX() + dx >= 0) Scene[0].addAction(Actions.moveTo(0, 0)); // if root left edge enters stage
				else if (Scene[0].getRoot().getX() + dx <= Scene[0].getWidth() - 445 - frontBackground.getWidth()) { // if root right edge is left of background + UI coverage
					Scene[0].addAction(Actions.moveTo(Scene[0].getWidth()- 445 - frontBackground.getWidth(), 0));
				}
				else Scene[0].addAction(Actions.moveBy(dx, 0, 0));
			}
		});
		frontBackground = new Image(new TiledDrawable(new TextureRegion(RopGame.assets.get("img/backgrounds/new front.png", Texture.class), 0, 224, 1280, 800)), Scaling.stretch);
		scene.addActor(frontBackground);
		frontBackground.setSize(1280 * 5, 800);
		scene.addActor(new GroupManager());
		return scene;
	}

	public void switchScreen(final int screen) {
		if (RopGame.DEBUG) assert(screen >= 0 && screen <= 3);
		((InputMultiplexer) Gdx.input.getInputProcessor()).removeProcessor(ActiveScene);
		ActiveScene = Scene[screen];
		((InputMultiplexer) Gdx.input.getInputProcessor()).addProcessor(ActiveScene);
	}

	@Override
	public void render(float delta) {
		for (Stage s: Scene) {
			s.act(delta);
		}
		UILayer.act(delta);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		ActiveScene.draw();
		UILayer.draw();
	}

	@Override
	public void resize(int width, int height) {
		float length = (float) width * 800f / (float) height;
		UILayer.setViewport(length, 800, true);
		UILayer.resize(length, 800);
		for (Stage s: Scene) {
			s.setViewport(length, 800, true);
		}
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Gets called when this goes to background. So, what?
	}

	@Override
	public void resume() {
		// TODO add call to showPauseOverlay

	}

	@Override
	public void dispose() {
		for (Stage s: Scene) {
			s.dispose();
		}
		ActiveScene = null;
		UILayer.dispose();
	}
	
	public Stage getScreen(int stage) {
		return Scene[stage];
	}
	
	public Stage getUI() {
		return UILayer;
	}
}
