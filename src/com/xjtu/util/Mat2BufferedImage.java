package com.xjtu.util;

import java.awt.image.BufferedImage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Mat2BufferedImage {
	
	/**
	 * 将Mat类型的图像转化为BufferedImg
	 * @param matrix Mat 
	 * @return BufferedImg
	 */
	public static BufferedImage mat2BufferedImage(Mat matrix) {
		
		//得到输入图像的行列数
		int cols=matrix.cols();
		int rows=matrix.rows();
		//得到mat图像中单个元素的大小，和CVType有关，一般的8UC1的都是返回1，C3返回的3，16UC1的就返回2
		int elementSize=(int)matrix.elemSize();
		//创建可以存放整个mat 元素的数组
		byte []data=new byte[cols*rows*elementSize];
		//获得输入图像的通道的数目
		int channels=matrix.channels();
		//这行代码获得所有像素点
		matrix.get(0, 0, data);
		//定义返回图像的类型
		int type;
		switch(channels) {
		//单通道
		case 1:
			type=BufferedImage.TYPE_BYTE_GRAY;
			break;
		//3通道
		case 3:
			type=BufferedImage.TYPE_3BYTE_BGR;
			//将mat中BGR 转化为  RGB
			byte tmp;
			for(int i=0; i<data.length; i+=3) {
				tmp=data[i];
				data[i]=data[i+2];
				data[i+2]=tmp;
			}
			break;
		default:
				System.out.println("Mat2BufferedImage.class,"+"通道数目不是1或者3");
				return null;
		}
		BufferedImage img=new BufferedImage(cols,rows,type);
		img.getRaster().setDataElements(0,0,cols,rows,data);
		return img;
	}
	
	/**
	 * 测试上面的方法
	 * @param args
	 */
	public static void main(String []args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String filePath="E:\\PigFace\\1\\1.jpg";
		Mat img=Imgcodecs.imread(filePath);
		Imshow.imshow(img, "dirty pig");
	}
}
