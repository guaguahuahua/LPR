package com.xjtu.util;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Resize {

	/**
	 * 对输入图像进行归一化，大小为指定的大小
	 * @param src	Mat	用户输入图像
	 * @return	Mat	归一化之后的图像
	 */
	public static Mat resize(Mat src) {
		//定义输出图像类型，因为要使用easyPR的图像库，所以要将图像的大小归一化为他的尺寸
		Mat dst=new Mat(36, 136, CvType.CV_8UC3); //36,136
		Imgproc.resize(src, dst, dst.size());
		return dst;
	}
}
