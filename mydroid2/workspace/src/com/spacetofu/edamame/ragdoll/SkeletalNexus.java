package com.spacetofu.edamame.ragdoll;

import java.util.ArrayList;

public class SkeletalNexus {
	
	public ArrayList<Bone> bones;
	
	public SkeletalNexus() {
		bones = new ArrayList<Bone>();
	}
	
	public void place(Bone bone) {
		bones.add(bone);
	}
	
	public void update() {
		
	}
	
	private void reach() {
		
	}
}
