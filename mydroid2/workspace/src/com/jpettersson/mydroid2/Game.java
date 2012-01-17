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

package com.jpettersson.mydroid2;

import java.lang.reflect.Array;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import org.example.touch.WrapMotionEvent;

import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GenericVertexController;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.PolygonManager;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureInfo;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

public class Game {
	
	private static final String TAG = "MyDroid";
	
	private GameActivity activity;
	
	private GLSurfaceView view = null;
	private Renderer renderer = null;
	private FrameBuffer fb;
	private World world = null;
	private Light sun = null;
	
	private boolean paused = false;
	private boolean stop = false;

	private Object3D sky;
	private Object3D pivot;
	private Doll droid;

	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	float[] mArr;

	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	
	float scale = 0;
	float realScale = 0;

	float camAngle = 0;
	float targetCamRad = 900;
	float camRad = 900;
	
	float targetPivotRotY = 0;
	float pivotRotY = 0;
	
	private SimpleVector sunColor;
	private SimpleVector cameraAnchor;
	
	float greenEffect = 0;
	float blueEffect = 0;
	float sunPos = 0;
	
	float deviceRoll = 0;
	float devicePitch = 0;
	float deviceRollTarget = 0;
	float devicePitchTarget = 0;
	
	public Game(GameActivity activity) {
		
		this.activity = activity;
		view = new GLSurfaceView(activity) {
			public boolean onTouchEvent(final MotionEvent rawEvent) {
		       
		                WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
		      
		                switch (event.getAction() & MotionEvent.ACTION_MASK) {
		                case MotionEvent.ACTION_DOWN:
		                   savedMatrix.set(matrix);
		                   start.set(event.getX(), event.getY());
		                 
		                   mode = DRAG;
		                   break;
		                case MotionEvent.ACTION_POINTER_DOWN:
		                   oldDist = spacing(event);
		                   if (oldDist > 10f) {
		                      savedMatrix.set(matrix);
		                      midPoint(mid, event);
		                      mode = ZOOM;
		                   }
		                   break;
		                case MotionEvent.ACTION_UP:
		                	
		                case MotionEvent.ACTION_POINTER_UP:
		                   mode = NONE;
		                   realScale += scale*3;
		                   realScale = (float)Math.max(Math.min(realScale, 18), -8);
		                   scale = 0;
		                   
		                   targetCamRad = 1500 + (realScale * 100);
		               
		                   break;
		                case MotionEvent.ACTION_MOVE:
		                   if (mode == DRAG) {
		                      // ...
		                      matrix.set(savedMatrix);
		                      matrix.postTranslate(event.getX() - start.x,
		                            event.getY() - start.y);
		                      
		                      matrix.getValues(mArr);
		                      targetPivotRotY = -(float)((mArr[Matrix.MTRANS_X]/view.getWidth()) * Math.PI*2);
		                   }
		                   else if (mode == ZOOM) {
		                      float newDist = spacing(event);
		                  
		                      if (newDist > 10f) {
		                         scale += (1-(-1f+((newDist / oldDist)*2)));
		                         
		                         oldDist = newDist;
		                      }
		                   }
		                   break;
		                }
		                
		        
		        try{
		        	Thread.sleep(128);
		        }catch(InterruptedException e) {
		        	Log.v(TAG, "MotionEvent error");
		        }
		                
		        return true;
		    }
			
		};
		
		view.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
				// back to Pixelflinger on some device (read: Samsung I7500)
				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});

		renderer = new Renderer();
		view.setRenderer(renderer);
		
	}
	
	public void pause() {
		renderer.pause();
		//view.onPause();
	}
	
	public void resume() {
		renderer.resume();
		//view.onResume();
	}
	
	public void stop() {
		//renderer.stop();
	}
	
	public class Renderer implements GLSurfaceView.Renderer  {

		public Renderer() {
			Config.maxPolysVisible = 5000;
			Config.farPlane = 20000;
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
		
		
		private void tileTexture(Object3D obj, float tileFactor) {
			PolygonManager pm = obj.getPolygonManager();

			int end = pm.getMaxPolygonID();
			for (int i = 0; i < end; i++) {
				SimpleVector uv0 = pm.getTextureUV(i, 0);
				SimpleVector uv1 = pm.getTextureUV(i, 1);
				SimpleVector uv2 = pm.getTextureUV(i, 2);

				uv0.scalarMul(tileFactor);
				uv1.scalarMul(tileFactor);
				uv2.scalarMul(tileFactor);

				int id = pm.getPolygonTexture(i);

				TextureInfo ti = new TextureInfo(id, uv0.x, uv0.y, uv1.x, uv1.y,
						uv2.x, uv2.y);
				pm.setPolygonTexture(i, ti);
			}
		}
		
		
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			
			
			cameraAnchor = new SimpleVector(0, -300, 0);
			
			mArr = new float[9];
			
			TextureManager.getInstance().flush();
			Resources res = activity.getResources();
			TextureManager tm = TextureManager.getInstance();

			Texture headTexture = new Texture(res.openRawResource(R.raw.droid_head_tex_2));
			tm.addTexture("head", headTexture);
			
			Texture worldTexture = new Texture(res.openRawResource(R.raw.cube_tex7));
			tm.addTexture("world", worldTexture);
			
			world = new World();
		
			pivot = Object3D.createDummyObj();
			
			Object3D parts[] = Loader.load3DS(res.openRawResource(R.raw.droid_008), 3);
			
			droid = new Doll(world, parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
			
			droid.body.rotateX((float)(-Math.PI/2));
			droid.head.setTexture("head");
			
			pivot.addChild(droid.body);
			
			sky = Loader.load3DS(res.openRawResource(R.raw.cube_001), 20)[0]; 
			sky.setTexture("world");
			sky.invert();
			sky.translate(0, -1630, 0);
			world.addObject(sky);
			
			//Light
			sunColor = new SimpleVector(255, 255, 255);
			world.setAmbientLight(40, 60, 80);
			world.buildAllObjects();
			sun = new Light(world);
			sun.setIntensity(sunColor.x, sunColor.y, sunColor.z);
			SimpleVector sv = new SimpleVector();
			sv.y -= 600;
			sv.z = -600;
			sv.x = -600;
			sun.setPosition(sv);
			
			//Camera
			Camera cam = world.getCamera();
			cam.setPosition(new SimpleVector(1100, -1000, 0));
			cam.setFOV(1.5f); //1.5
			
			droid.calculatePivots();
			droid.randomizeMovements();
			
			cam.lookAt(droid.body.getTransformedCenter());
		}

		public void onDrawFrame(GL10 gl) {

			try {
				if (!stop) {
					if (paused) {
						Thread.sleep(500);
					} else {
						Camera cam = world.getCamera();
						
						deviceRoll += (deviceRollTarget - deviceRoll)/4;
						devicePitch += (devicePitchTarget - devicePitch)/4;
						
						pivotRotY += (targetPivotRotY-pivotRotY) / 4;
						
						camRad += (targetCamRad - camRad)/2;
						
						double x = Math.sin(-pivotRotY) * camRad;
						double z = Math.cos(-pivotRotY) * camRad;
						cam.setPosition((float)x, 0 + devicePitch, (float)z); 
						
						cameraAnchor.x = deviceRoll;
						
						cam.lookAt(cameraAnchor);
						
						droid.animate();
						
						fb.clear();

						world.renderScene(fb);
						world.draw(fb);
						
						
						
						fb.display();
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
	}
	
	public GLSurfaceView getSurfaceView() {
		return view;
	}
	
	
	/** Determine the space between the first two fingers */
	private float spacing(WrapMotionEvent event) {
	   // ...
	   float x = event.getX(0) - event.getX(1);
	   float y = event.getY(0) - event.getY(1);
	   return FloatMath.sqrt(x * x + y * y);
	}

 /** Calculate the mid point of the first two fingers */  
	private void midPoint(PointF point, WrapMotionEvent event) {
	   // ...
	   float x = event.getX(0) + event.getX(1);
	   float y = event.getY(0) + event.getY(1);
	   point.set(x / 2, y / 2);
	}
	
	public void updateOrientation(float roll, float pitch) {
		deviceRollTarget = Math.max(Math.min(roll * 10, 200),-200);
		devicePitchTarget = -(pitch/-60) * 600;//Math.max(Math.min(pitch * 10, 200),-200);
	}
	
}