package com.xjtu.other;

public class MeanValueMethod implements Calculate{
	
	/**
	 * ƽ��ֵ��
	 */
	public int calcu(double r, double g, double b) {
		int mean=(int) ((r+g+b)/3);
		return mean;
	}
	
}
