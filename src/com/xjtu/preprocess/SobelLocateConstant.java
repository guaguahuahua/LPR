package com.xjtu.preprocess;

import org.opencv.core.CvType;

public final class SobelLocateConstant {
	//高斯模糊的半径
	private static final int gaussianBlurSize=27; //9
	//调用sobel算子的图像深度的设置
	private static final int ddepth=CvType.CV_16S;
	//图像的放缩比例
	private static final int scale=1;
	private static final int delta=0;
	//调用形态学处理图像时的矩形模板的大小
	private static final int morphSizeWidth=17; //17
	private static final int morphSizeHeight=3; //3
	
	public static int getGaussianblursize() {
		return gaussianBlurSize;
	}
	public static int getDdepth() {
		return ddepth;
	}
	public static int getScale() {
		return scale;
	}
	public static int getDelta() {
		return delta;
	}
	public static int getMorphsizewidth() {
		return morphSizeWidth;
	}
	public static int getMorphsizeheight() {
		return morphSizeHeight;
	}
}
