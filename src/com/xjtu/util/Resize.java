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
		//�������ͼ�����ͣ���ΪҪʹ��easyPR��ͼ��⣬����Ҫ��ͼ��Ĵ�С��һ��Ϊ���ĳߴ�
		Mat dst=new Mat(36, 136, CvType.CV_8UC3); //36,136
		Imgproc.resize(src, dst, dst.size());
		return dst;
	}
}
