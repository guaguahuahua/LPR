package com.xjtu.other;

public class MaxValueMethod implements Calculate{

	@Override
	public int calcu(double r, double g, double b) {
		// TODO Auto-generated method stub
		int max=(int) Math.max(Math.max(r, g), b);
		return max;
	}

}
