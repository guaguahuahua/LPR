package com.xjtu.util;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;

public class Imshow {
	/**
	 * �������ͼ�����������ڴ���չʾ
	 * @param original
	 */
	public static void imshow(Mat original) {
		BufferedImage bfImage=Mat2BufferedImage.mat2BufferedImage(original);
		ShowImage.showImage(bfImage);
	}
	
	/**
	 * ������ʾ������һ�����أ���Ҫ����ԭ���Ļ���������˴�������֣����Է�������ֿ�ÿ���������Ǹ������ɵ�
	 * @param original ����ͼ��
	 * @param name ��������� 	�������򣺡���ǰ��ʾ������.������
	 */
	public static void imshow(Mat original, String name) {
		BufferedImage bfImage=Mat2BufferedImage.mat2BufferedImage(original);
		ShowImage.showImage(bfImage, name);
	}
}
