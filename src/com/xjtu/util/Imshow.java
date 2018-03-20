package com.xjtu.util;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;

public class Imshow {
	/**
	 * 对输入的图像进行输出，在窗口展示
	 * @param original
	 */
	public static void imshow(Mat original) {
		BufferedImage bfImage=Mat2BufferedImage.mat2BufferedImage(original);
		ShowImage.showImage(bfImage);
	}
	
	/**
	 * 这是显示方法的一个重载，主要是在原来的基础上添加了窗体的名字，可以方便的区分开每个窗体是那个类生成的
	 * @param original 输入图像
	 * @param name 窗体的名字 	命名规则：“当前显示的内容.类名”
	 */
	public static void imshow(Mat original, String name) {
		BufferedImage bfImage=Mat2BufferedImage.mat2BufferedImage(original);
		ShowImage.showImage(bfImage, name);
	}
}
