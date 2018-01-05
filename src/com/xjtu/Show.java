package com.xjtu;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.xjtu.util.Imshow;

public class Show {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat original=Imgcodecs.imread("E:/ImagesFromAndroid/abc.jpg");
		Imshow.imshow(original);
	}

}
