package com.xjtu.util;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Resize1 {

	/**
	 * 将输入图像转化为固定大小
	 * @param input Mat 
	 * 					输入图像
	 * @return Mat
	 *  				格式化以后的图像
	 */
	public static Mat resize1(Mat input) {
		
		Mat dst=new Mat(20, 20, CvType.CV_8UC1); //36,136
		Imgproc.resize(input, dst, dst.size());
		return dst;
	}
}
