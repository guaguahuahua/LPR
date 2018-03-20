package com.xjtu.other;

public interface Calculate {
	
	/**
	 * 灰度化
	 * 这里的参数均为double，是因为Opencv内置Mat原因导致；实际我们原始图像和获得灰度值都是整型的，
	 * @param r	double 
	 * @param g	double
	 * @param b	double
	 * @return	int	最终的灰度值
	 */
	public int calcu(double r, double g, double b);
}
