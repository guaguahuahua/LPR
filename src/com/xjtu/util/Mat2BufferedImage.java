package com.xjtu.util;

import java.awt.image.BufferedImage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Mat2BufferedImage {
	
	/**
	 * ��Mat���͵�ͼ��ת��ΪBufferedImg
	 * @param matrix Mat 
	 * @return BufferedImg
	 */
	public static BufferedImage mat2BufferedImage(Mat matrix) {
		
		//�õ�����ͼ���������
		int cols=matrix.cols();
		int rows=matrix.rows();
		//�õ�matͼ���е���Ԫ�صĴ�С����CVType�йأ�һ���8UC1�Ķ��Ƿ���1��C3���ص�3��16UC1�ľͷ���2
		int elementSize=(int)matrix.elemSize();
		//�������Դ������mat Ԫ�ص�����
		byte []data=new byte[cols*rows*elementSize];
		//�������ͼ���ͨ������Ŀ
		int channels=matrix.channels();
		//���д������������ص�
		matrix.get(0, 0, data);
		//���巵��ͼ�������
		int type;
		switch(channels) {
		//��ͨ��
		case 1:
			type=BufferedImage.TYPE_BYTE_GRAY;
			break;
		//3ͨ��
		case 3:
			type=BufferedImage.TYPE_3BYTE_BGR;
			//��mat��BGR ת��Ϊ  RGB
			byte tmp;
			for(int i=0; i<data.length; i+=3) {
				tmp=data[i];
				data[i]=data[i+2];
				data[i+2]=tmp;
			}
			break;
		default:
				System.out.println("Mat2BufferedImage.class,"+"ͨ����Ŀ����1����3");
				return null;
		}
		BufferedImage img=new BufferedImage(cols,rows,type);
		img.getRaster().setDataElements(0,0,cols,rows,data);
		return img;
	}
	
	/**
	 * ��������ķ���
	 * @param args
	 */
	public static void main(String []args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String filePath="E:\\PigFace\\1\\1.jpg";
		Mat img=Imgcodecs.imread(filePath);
		Imshow.imshow(img, "dirty pig");
	}
}
