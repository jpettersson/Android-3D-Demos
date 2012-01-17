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

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.spacetofu.math.LinearVectorInterpolator;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public class Doll {
	
	public Object3D body;
	
	public Object3D head;
	public Object3D torso;
	public Object3D lArm;
	public Object3D rArm;
	public Object3D lLeg;
	public Object3D rLeg;
	
	private Object3D[] parts;
	
	private Pose[] poses;
	
	private LinearVectorInterpolator iHeadRotation = new LinearVectorInterpolator(4);
	private LinearVectorInterpolator iLArmRotation = new LinearVectorInterpolator(4);
	private LinearVectorInterpolator iRArmRotation = new LinearVectorInterpolator(4);
	
	private float headRotZ = 0;

	private Timer timer;
	
	public Doll(World world, Object3D head, Object3D torso, Object3D lArm, Object3D rArm, Object3D lLeg, Object3D rLeg) {
		
		
		parts = new Object3D[6];
		
		this.head = head;
		this.torso = torso;
		this.lArm = lArm;
		this.rArm = rArm;
		this.lLeg = lLeg;
		this.rLeg = rLeg;
		
		body = Object3D.createDummyObj();
		
		body.addChild(head);
		body.addChild(torso);
		torso.addChild(lArm);
		torso.addChild(rArm);
		torso.addChild(lLeg);
		torso.addChild(rLeg);
		
		world.addObject(head);
		world.addObject(torso);
		world.addObject(lArm);
		world.addObject(rArm);
		world.addObject(lLeg);
		world.addObject(rLeg);
		
		parts[0] = head;
		parts[1] = torso;
		parts[2] = lArm;
		parts[3] = rArm;
		parts[4] = lLeg;
		parts[5] = rLeg;
		
		poses = new Pose[5];
		poses[0] = new Pose(new SimpleVector(0, 0, 0), new SimpleVector(0, 0, 0));
		poses[1] = new Pose(new SimpleVector(Math.PI/8, 0, 0), new SimpleVector(Math.PI/8, 0, 0));
		poses[2] = new Pose(new SimpleVector(-Math.PI/8, 0, 0), new SimpleVector(-Math.PI/8, 0, 0));
		poses[3] = new Pose(new SimpleVector(0, 0, 0), new SimpleVector(Math.PI/8, 0, 0));
		poses[4] = new Pose(new SimpleVector(-Math.PI/8, 0, 0), new SimpleVector(0, 0, 0));
	}
	
	public void calculatePivots() {
		lArm.setRotationPivot(new SimpleVector(-300, 0, 300));
		rArm.setRotationPivot(new SimpleVector(300, 0, 300));
	}
	
	public void interact(SimpleVector touchPoint) {
		for(int i=0;i<parts.length;i++) {
			if(hitTest(parts[i], touchPoint)) {
				select(parts[i]);
			}
		}
	}
	
	private Boolean hitTest(Object3D obj, SimpleVector touchPoint) {
		
		return false;
	}
	
	private void select(Object3D obj) {
		Log.i("Doll", "Select : " + obj);
	}
	
	public void animate() {
		iHeadRotation.step(LinearVectorInterpolator.STEP_Z);
		iLArmRotation.step(LinearVectorInterpolator.STEP_3D);
		iRArmRotation.step(LinearVectorInterpolator.STEP_3D);
		
		head.clearRotation();
		head.rotateZ(iHeadRotation.p.z); //(float)(-Math.PI/4 + ((Math.cos(headRotZ) * Math.PI)/Math.PI*2)*Math.PI/4)
		
		lArm.clearRotation();
		lArm.rotateX(iLArmRotation.p.x);
		lArm.rotateY(iLArmRotation.p.y);
		lArm.rotateZ(iLArmRotation.p.z);
		
		rArm.clearRotation();
		rArm.rotateX(iRArmRotation.p.x);
		rArm.rotateY(iRArmRotation.p.y);
		rArm.rotateZ(iRArmRotation.p.z);
	}
	
	public void randomizeMovements() {
		timer = new Timer();
		timer.schedule(new RandomPoseTask(), 3000l);
	}
	
	class RandomPoseTask extends TimerTask {

		@Override
		public void run() {
			
			iHeadRotation.t.z = (float)(-Math.PI/4 + ((Math.cos(Math.random()) * Math.PI)/Math.PI*2)*Math.PI/4);
			
			Pose pose = poses[(int) Math.floor(Math.random()*poses.length)];
			iLArmRotation.t.x = pose.lArmRot.x;
			iLArmRotation.t.y = pose.lArmRot.y;
			iLArmRotation.t.z = pose.lArmRot.z;
			
			iRArmRotation.t.x = pose.rArmRot.x;
			iRArmRotation.t.y = pose.rArmRot.y;
			iRArmRotation.t.z = pose.rArmRot.z;
			
			//iRArmRotation.t.x = -iRArmRotation.t.x;
			
			randomizeMovements();
		}
		
	}

	private class Pose {
		public SimpleVector lArmRot;
		public SimpleVector rArmRot;
		
		public Pose(SimpleVector lArmRot, SimpleVector rArmRot) {
			this.lArmRot = lArmRot;
			this.rArmRot = rArmRot;
		}
	}
}

