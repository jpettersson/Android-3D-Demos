package com.spacetofu.edamame.ragdoll;

import com.threed.jpct.SimpleVector;

public class Bone {
	
	public SimpleVector position;
	public SimpleVector rotation;
	public SimpleVector rotationConstraint;
	
	public Bone() {
		position = new SimpleVector();
		rotation = new SimpleVector();
		rotationConstraint = new SimpleVector();
	}

}
