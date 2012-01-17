package com.spacetofu.math;

import com.threed.jpct.SimpleVector;

public class LinearVectorInterpolator {
	public SimpleVector p;
	public SimpleVector t;
	
	public float ease;
	
	public static int STEP_X=0;
	public static int STEP_Y=1;
	public static int STEP_Z=2;
	public static int STEP_2D=3;
	public static int STEP_3D=4;
	
	public LinearVectorInterpolator(float ease) {
		this.ease = ease;
		p = new SimpleVector(0, 0, 0);
		t = new SimpleVector(0, 0, 0);
	}
	
	public void step(int stepMode) {
		p.x += (t.x-p.x)/ease;
		p.y += (t.y-p.y)/ease;
		p.z += (t.z-p.z)/ease;
	}
}
