package com.xjtu.svm;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class ExtractFeature {

	/**
	 * ��ȡrgbMat�е������� ��172ά�ȵ���������
	 * @param rgbMat Mat
	 * 					����Ĳ�ɫͼ��
	 * @return Mat
	 * 					���س�ȡԭ��ֵͼ�����������mat����ʽ����
	 */
	public static Mat extractFeature(Mat rgbMat) {
		Mat feature=new Mat(1, rgbMat.rows()+rgbMat.cols(), CvType.CV_8UC1);
		//rgbͼ�� ת��Ϊ�Ҷ�ͼ
		Mat grayMat=new Mat();
		Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_BGR2GRAY);
		//�Ҷ�ͼת��ֵͼ
		Mat binaryMat=new Mat();
		Imgproc.adaptiveThreshold(grayMat, binaryMat, 1, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 25, 10);
		//ͳ��ˮƽ�ʹ�ֱ�����1�ĸ���
		//��ֱ����
		int i;
		for(i=0; i<binaryMat.rows(); i++) {
			int count=0;
			for(int j=0; j<binaryMat.cols(); j++) {
				count+=binaryMat.get(i, j)[0];
//				System.out.println(binaryMat.get(i, j)[0]);
			}
			feature.put(0, i, new byte[] {(byte)count});
		}
		//ˮƽ����
		for(int col=0; col<binaryMat.cols(); col++) {
			int count=0;
			for(int row=0; row<binaryMat.rows(); row++) {
				count+=binaryMat.get(row, col)[0];
			}
			feature.put(0, i++, count);
		}
		//��mat����ʽ����ȡ���������أ�ˮƽ+��ֱ����
		return feature;
	}
	
	/**
	 * �׶��в���
	 * @param args
	 */
	public static void main(String []args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String file="E:/HasPlate/plate (1).jpg";
		Mat original=Imgcodecs.imread(file);
		//����һ����������
		Mat feature=new Mat(1, original.rows()+original.cols(), CvType.CV_8UC1);
		feature=extractFeature(original);
		//test para ���feature�е�����Ԫ��
		System.out.println("feature.size: "+feature.rows()+","+feature.cols());
		for(int j=0; j<feature.cols(); j++) {
			System.out.print(feature.get(0, j)[0]+"\t");
		}
		System.out.println();
	}
}
