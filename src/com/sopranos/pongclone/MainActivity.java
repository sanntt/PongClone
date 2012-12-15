package com.sopranos.pongclone;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

public class MainActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener {

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	
	private static final int VELOCITY = 30;
	
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mCircleFaceTextureRegion;
	
	@Override
	public EngineOptions onCreateEngineOptions() {

		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(), camera);
	
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onCreateResources() {
		
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 128, TextureOptions.BILINEAR);
		
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 0, 32, 2, 1); // 64x32
		
		this.mBitmapTextureAtlas.load();
		
	}

	@Override
	protected Scene onCreateScene() {

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));
		this.mScene.setOnSceneTouchListener(this);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);

		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		Body leftWall = PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		Body rightWall = PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		
		leftWall.setUserData("leftWall");
		leftWall.setActive(true);
		rightWall.setUserData("rightWall");
		rightWall.setActive(true);
		
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		final AnimatedSprite face = new AnimatedSprite(CAMERA_WIDTH/2, CAMERA_HEIGHT/2, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
		final Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
		body.setActive(true);
		body.setUserData("pelotita");
		body.setLinearVelocity(10, 0);	
		

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		
		this.mScene.attachChild(face);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, false));

		return this.mScene;
		
	}
	
	private ContactListener createContactListener()
    {
        ContactListener contactListener = new ContactListener()
        {
            @Override
            public void beginContact(Contact contact)
            {
            	final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                
                if (x2.getBody().getUserData().equals("pelotita") && x1.getBody().getUserData().equals("leftwall")) {
                	x2.getBody().setLinearVelocity(VELOCITY , x2.getBody().getLinearVelocity().y);
                } else if (x2.getBody().getUserData().equals("pelotita") && x1.getBody().getUserData().equals("rightwall")) {
                	x2.getBody().setLinearVelocity(VELOCITY * -1, x2.getBody().getLinearVelocity().y);
                } else if (x1.getBody().getUserData().equals("pelotita") && x2.getBody().getUserData().equals("leftwall")) {
                	x1.getBody().setLinearVelocity(VELOCITY , x2.getBody().getLinearVelocity().y);
                } else if (x1.getBody().getUserData().equals("pelotita") && x1.getBody().getUserData().equals("rightwall")) {
                	x1.getBody().setLinearVelocity(VELOCITY * -1, x1.getBody().getLinearVelocity().y);
                } 
                
            }
 
            @Override
            public void endContact(Contact contact)
            {
            	 
            }
 
            @Override
            public void preSolve(Contact contact, Manifold oldManifold)
            {
                   
            }
 
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse)
            {
               
            }
        };
        return contactListener;
    }

}
