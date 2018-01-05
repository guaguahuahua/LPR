package com.xjtu.util;

import org.opencv.core.RotatedRect;

public class VerifySize {
	static final double error=0.9;
	static final double aspect=3.75;
	static final int verifyMin=1;
	static final int verifyMax=120;
	/**
	 * 对传进来的矩形进行面积阈值的筛选
	 * @param mr
	 * @return boolean
	 */
	public static boolean verifySize(RotatedRect mr) {
		//面积最大最小值
//		int min=34*8*verifyMin;
//		int max=34*8*verifyMax;
		
		int min=44*14*verifyMin;
		int max=44*14*verifyMax;
		//宽高比的最大最小值
		double rmin=aspect*(1-error);
		double rmax=aspect*(1+error);
		//传入参数的面积
		double s=mr.size.height*mr.size.width;
		double r=mr.size.width/mr.size.height;
		if(r<1) {
			r=mr.size.height/mr.size.width;
		}
//		System.out.println("宽高之比："+r);
//		System.out.println("面积："+s);
		return (r>=rmin && r<=rmax) && (s>=min && s<=max);
	}

}
