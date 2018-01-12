package com.xjtu.util;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Resize1 {

	/**
	 * ������ͼ��ת��Ϊ�̶���С
	 * @param input Mat 
	 * 					����ͼ��
	 * @return Mat
	 *  				��ʽ���Ժ��ͼ��
	 */
	public static Mat resize1(Mat input) {
		
		Mat dst=new Mat(20, 20, CvType.CV_8UC1); //36,136
		Imgproc.resize(input, dst, dst.size());
		return dst;
	}
}
