package ml.sakii.pandadodge;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Main extends ApplicationAdapter implements InputProcessor{
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private int width, height;
	private Texture panda, log, water, panda_jumping, panda_falling;
	private Rectangle pandaRect;
	private Rectangle activeObstacle;
	private boolean running;
	private float velocity;
	private float gravity;
	private boolean floating;
	private float jumpforce;
	private int terrainSpeed;
	private boolean previousTop;
	
	private boolean underwater;
	int sourceX;
	float difficulty=2f;
	BitmapFont font; 
	
	private GlyphLayout easy, medium, hard;
	private Rectangle easyBox, mediumBox, hardBox;
//	private Rec easy, medium, hard;
	

	
	int points;
	@Override
	public void create () {
		font = new BitmapFont();
		font.getData().setScale(3);
		batch = new SpriteBatch();
		
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, width, height);
		
		panda = new Texture(Gdx.files.internal("panda.png"));
		panda_jumping = new Texture(Gdx.files.internal("panda_jumping.png"));
		panda_falling = new Texture(Gdx.files.internal("panda_falling.png"));
		log = new Texture(Gdx.files.internal("log.png"));
		water = new Texture(Gdx.files.internal("water.jpg"));
		water.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		pandaRect = new Rectangle(width/10, height/2-panda.getHeight()/2, height*0.2f, height*0.2f);
			
		gravity=height*1.4f;  // 1000
		jumpforce=height; // 700
		terrainSpeed = (int)(width/3);  // /3
		
		easy = new GlyphLayout(font, "Easy");
		medium = new GlyphLayout(font, "Medium");
		hard = new GlyphLayout(font, "Hard");
		
		easyBox = new Rectangle(width/4-easy.width/2 , height/2-easy.height/2, easy.width, easy.height);
		mediumBox = new Rectangle(width/4*2-medium.width/2 , height/2-medium.height/2, medium.width, medium.height);
		hardBox = new Rectangle(width/4*3-hard.width/2 , height/2-hard.height/2, hard.width, hard.height);
		
		Gdx.input.setInputProcessor(this);
		reset();
		running=false;
		

	}
	
	private void reset(){
		points = -1;
		activeObstacle = newObstacle();
		running=true;
		velocity=0f;
		floating=false;
		underwater=false;
		sourceX=0;
		previousTop=false;
		pandaRect.y = height/2-panda.getHeight()/2;
		
	}

	@Override
	public void render () {
		if(!running){
			Gdx.gl.glClearColor(1f, 0f, 0f, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			//camera.update();
			batch.begin();
			font.draw(batch, "PandaDodge\nPoints: " + points + "", 0, height);
			font.draw(batch, "Easy", easyBox.x , easyBox.y+easy.height);
			font.draw(batch, "Medium", mediumBox.x , mediumBox.y+medium.height);
			font.draw(batch, "Hard", hardBox.x , hardBox.y+hard.height);
			batch.end();

			return;
		}
		
		
		Gdx.gl.glClearColor(0.5f, 0.5f, 1.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		
		if(floating){
			if(!underwater){
				velocity -= gravity * Gdx.graphics.getDeltaTime()*difficulty;
				if(pandaRect.y<height/2-panda.getHeight()/2){
					underwater=true;
					//velocity=0;
				}
			}else{
				velocity += gravity * Gdx.graphics.getDeltaTime()*difficulty;
				if(pandaRect.y>height/2-panda.getHeight()/2){
					underwater=false;
					velocity=0;
					floating =false;
					pandaRect.y = height/2-panda.getHeight()/2;
				}
				
			}
			
		}
		pandaRect.y += velocity*Gdx.graphics.getDeltaTime()*difficulty;
		
		activeObstacle.x -= terrainSpeed*Gdx.graphics.getDeltaTime()*difficulty;
		if(activeObstacle.x<-activeObstacle.width){
			activeObstacle = newObstacle();
		}
		
		if(pandaRect.overlaps(activeObstacle)){
			running = false;
			Gdx.input.vibrate(200);
			return;
		}



		sourceX += terrainSpeed*Gdx.graphics.getDeltaTime()*difficulty;

		while(sourceX>water.getWidth()){
			sourceX-=water.getWidth();
		}
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(water, 0, 0, sourceX, 0, width, height/2-panda.getHeight()/2);
		if(velocity>0.1){
			batch.draw(panda_jumping, pandaRect.x, pandaRect.y, height*0.2f, height*0.2f);
		}else if(velocity < -0.1){
			batch.draw(panda_falling, pandaRect.x, pandaRect.y, height*0.2f, height*0.2f);	
		}else{
			batch.draw(panda, pandaRect.x, pandaRect.y, height*0.2f, height*0.2f);
		}
		batch.draw(log, activeObstacle.x, activeObstacle.y, activeObstacle.width, activeObstacle.height);
		font.draw(batch, ""+points, 0, height);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		panda.dispose();
		log.dispose();
		water.dispose();
		panda_falling.dispose();
		panda_jumping.dispose();
		font.dispose();
		
	}
	
	private Rectangle newObstacle(){
		float x=width, y;

		float owidth=MathUtils.random(10,100);
		float oheight=MathUtils.random(height/1.5f,height/2.2f);
		if(MathUtils.randomBoolean()){
			//System.out.print("TOP:");
			y=height-oheight;
			if(!previousTop){
				x*=1.5f;
			}
			previousTop=true;
		}else{
			//System.out.print("BOTTOM:");
			y=0;
			previousTop=false;
		}
		points++;
		return new Rectangle(x, y, owidth, oheight);
	}
	
	private boolean checkIfClicked (Rectangle button) {
		float mx=camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f)).x;
		float my=camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f)).y;

		if(button.contains(mx, my)){
			System.out.println("Pressed: " + button.toString());
			System.out.println("X: " + mx + ", Y: " + my);

        	return true;
		}else{
			System.out.println("Not pressed: " + button.toString());
			System.out.println("X: " + mx + ", Y: " + my);
        return false;
		}
    }

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(!floating && running){
			floating = true;
			velocity=jumpforce;
		}
		
		if(!running){
			
			if(checkIfClicked(easyBox)){
				reset();
				difficulty=1f;
			}else if(checkIfClicked(mediumBox)){
				reset();
				difficulty=1.5f;
			}else if(checkIfClicked(hardBox)){
				reset();
				difficulty=2f;
			}
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}


}
