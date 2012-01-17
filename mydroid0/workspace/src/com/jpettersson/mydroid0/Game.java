/*
Copyright 2011 Jonathan A Pettersson  

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.jpettersson.mydroid0;

import java.util.ArrayList;
import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GenericVertexController;
import com.threed.jpct.IRenderHook;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.FloatMath;

public class Game implements GLSurfaceView.Renderer {
	
	private GameActivity activity;
	
	private FrameBuffer fb = null;
	private World world = null;
	
	private Object3D plane = null;
	private Texture font = null;

	private ArrayList<GameObject> primitives;
	
	private int fps = 0;
	private int lfps = 0;

	private long time = System.currentTimeMillis();

	private Light sun = null;

	private boolean stop = false;

	private float ind;

	private boolean deSer = true;
	
	private boolean paused = false;
	
	private double camAngle = 0;
	
	
	private float inR = 1000;
	private float inG = 255;
	private float inB = 255;
	
	public Game(GameActivity activity) {
		this.activity = activity;
		Config.maxPolysVisible = 5000;
		Config.farPlane = 1500;
	}
	
	public void stop() {
		stop = true;
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		if (fb != null) {
			fb.dispose();
		}
		fb = new FrameBuffer(gl, w, h);
	}
	
	public void pause() {
		paused = true;
	}
	
	public void resume() {
		paused = false;
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		TextureManager.getInstance().flush();
		world = new World();
		Resources res = activity.getResources();

		TextureManager tm = TextureManager.getInstance();
		Texture planetex = new Texture(res.openRawResource(R.raw.planetex));
		
		Texture red = new Texture(10, 10, new RGBColor(255, 97, 160, 100));

		font = new Texture(res.openRawResource(R.raw.numbers));

		tm.addTexture("grassy", planetex);
		tm.addTexture("red", red);

		plane = Primitives.getPlane(20, 60);

		plane.setTexture("grassy");

		plane.getMesh().setVertexController(new Mod(), false);
		plane.getMesh().applyVertexController();
		plane.getMesh().removeVertexController();

		
		plane.rotateX((float) Math.PI / 2f);
		plane.setName("plane");
		world.addObject(plane);
		
		primitives = new ArrayList<GameObject>();
		
		for(int i=0;i<12;i++) {
			Object3D p = Primitives.getSphere(6, 20f);
			p.setName("primitive" + i);
			p.setTexture("red");
			p.translate(i * 90, -90, 0);

			p.rotateX((float)Math.random()*360);
			p.rotateY((float)Math.random()*360);
			p.rotateZ((float)Math.random()*360);
			
			p.setRenderHook(new IRenderHook() {

				@Override
				public void afterRendering(int arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void beforeRendering(int arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onDispose() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public boolean repeatRendering() {
					// TODO Auto-generated method stub
					return false;
				}});
			
			world.addObject(p);
			
			GameObject po = new GameObject(0, 0, 0, i * 70);
			po.ob3d = p;
			
			primitives.add(po);
		}

		world.setAmbientLight(20, 20, 20);
		world.buildAllObjects();

		sun = new Light(world);

		Camera cam = world.getCamera();
		cam.moveCamera(Camera.CAMERA_MOVEOUT, 250);
		cam.moveCamera(Camera.CAMERA_MOVEUP, 100);
		
		cam.lookAt(plane.getTransformedCenter());

		cam.setFOV(1.5f);
		sun.setIntensity(inR, inG, inB);
	
		SimpleVector sv = new SimpleVector();
		sv.set(plane.getTransformedCenter());
		sv.y -= 300;
		sv.x -= 100;
		sv.z += 200;
		sun.setPosition(sv);
	}

	public void onDrawFrame(GL10 gl) {

		try {
			if (!stop) {
				if (paused) {
					Thread.sleep(500);
				} else {
					Camera cam = world.getCamera();
					
					fb.clear();
					world.renderScene(fb);
					world.draw(fb);
					blitNumber(lfps, 5, 5);

					fb.display();

					sun.rotate(new SimpleVector(0, 0.05f, 0), plane.getTransformedCenter());
					sun.setIntensity(inR, inG, inB);
					
					Iterator<GameObject> iterator = primitives.iterator();
	
					while(iterator.hasNext()) {
						GameObject next = iterator.next();
						
						next.ay += 0.1 * (1-(next.rad / 500));
						double nx = Math.sin(next.ay) * next.rad;
						double nz = Math.cos(next.ay) * next.rad;
						
						next.ob3d.clearTranslation();
						next.ob3d.translate((float)nx, -90f, (float)nz);
	
					}
					
					camAngle -= .005;
					int rad = 400;
					double x = Math.sin(camAngle) * rad;
					double z = Math.cos(camAngle) * rad;
					double fl = Math.cos(camAngle) * 100;
					cam.setPosition((float)x, (float)(-250f + fl), (float)z);
					
					cam.lookAt(plane.getTransformedCenter());
					
					if (System.currentTimeMillis() - time >= 1000) {
						lfps = (fps + lfps) >> 1;
						fps = 0;
						time = System.currentTimeMillis();
					}
					fps++;
					ind += 0.02f;
					if (ind > 1) {
						ind -= 1;
					}
				}
			} else {
				if (fb != null) {
					fb.dispose();
					fb = null;
				}
			}
		} catch (Exception e) {
			Logger.log("Drawing thread terminated!", Logger.MESSAGE);
		}
	}

	private class Mod extends GenericVertexController {
		private static final long serialVersionUID = 1L;

		public void apply() {
			SimpleVector[] s = getSourceMesh();
			SimpleVector[] d = getDestinationMesh();
			for (int i = 0; i < s.length; i++) {
				d[i].z = (float)(Math.random() * -200);
				d[i].x = s[i].x;
				d[i].y = s[i].y;
			}
		}
	}

	private void blitNumber(int number, int x, int y) {
		if (font != null) {
			String sNum = Integer.toString(number);
			for (int i = 0; i < sNum.length(); i++) {
				char cNum = sNum.charAt(i);
				int iNum = cNum - 48;
				fb.blit(font, iNum * 5, 0, x, y, 5, 9, FrameBuffer.TRANSPARENT_BLITTING);
				x += 5;
			}
		}
	}
	
	public void setColor(float r, float g, float b) {
		inR = r;
		inG = g;
		inB = b;
    }
	
	public class GameObject {
		
		public Object3D ob3d;
		
		public float ax = 0;
		public float ay = 0;
		public float az = 0;
		
		public float rad = 0;
		
		public GameObject(float ax, float ay, float az, float rad) {
			this.ax = ax;
			this.ay = ay;
			this.az = az;
			this.rad = rad;
		}
		
	}
	
	
	
}
