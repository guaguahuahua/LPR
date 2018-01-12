package com.xjtu.util;

import org.opencv.core.RotatedRect;

public class VerifySize {
	private static final double error=0.9;  //允许误差波动的范围
	private static final double aspect=3.75;//长宽比   
	private static final int verifyMin=1;   //面积最小倍率
	private static final int verifyMax=120; //最大的倍率，这个是经过实验调整得到的结果，可能需要调整
	
	/**
	 * 对传进来的矩形进行面积阈值的筛选
	 * @param mr RotatedRect 
	 * 				可能为车牌的轮廓
	 * @return boolean
	 *     			是否为车牌轮廓，是（true），否（false）
	 */
	public static boolean verifySize(RotatedRect mr) {
		//面积最大最小值
		//我国车牌的大小是长为44cm，高度为14cm，在验证是否为车牌图像的时候，
		//需要用这个矩形的面积来作为一阈值进行轮廓筛选
		int min=44*14*verifyMin; //最小的面积
		int max=44*14*verifyMax; //最大的面积
		//宽高比的最大最小值
		double rmin=aspect*(1-error);
		double rmax=aspect*(1+error);
		//传入参数的面积
		double s=mr.size.height*mr.size.width;
		//传入矩形的长宽比
		double r=mr.size.width/mr.size.height;
		//对长宽比进行一个调整，这里时发生了旋转的90度的情形
		if(r<1) {
			r=mr.size.height/mr.size.width;
		}
		//如果该矩形区域的面积在阈值范围内，长宽比也在范围内，返回true，否则返回false
		return (r>=rmin && r<=rmax) && (s>=min && s<=max);
	}

}
