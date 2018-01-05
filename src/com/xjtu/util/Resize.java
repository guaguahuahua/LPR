package com.xjtu.util;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Resize {
	/**
	 * @param src
	 * @param dst
	 */
	public static Mat resize(Mat src) {
		//定义输出图像类型，因为要使用easyPR的图像库，所以要将图像的大小归一化为他的尺寸
		Mat dst=new Mat(36, 136, CvType.CV_8UC3); //36,136
		Imgproc.resize(src, dst, dst.size());
		return dst;
	}
}
