package com.xjtu.other;

public class WeightedMeanMethod implements Calculate{

	@Override
	public int calcu(double r, double g, double b) {
		// TODO Auto-generated method stub
		int gray=((int)(54*r+183*g+18*b))>>8;
		return gray;
	}

}
