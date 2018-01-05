package com.xjtu.ann;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ExtractANNFeature {

	/**
	 * ��ȡ�����ַ�������,
	 * ע�⣺����mat��3ͨ����
	 * @param mat
	 * @return 
	 */
	public static Mat extractANNFeature(Mat mat) {
		//��ų�ȡ����������
		Mat features=new Mat(1, 60, CvType.CV_32FC1);
		//�ҶȻ�,����ԭͼ����������ͨ�Ķ�ֵͼ��ֱ�ӻҶȻ�����ɵ�ͨ���Ķ�ֵͼ������˶�ֵ�����������ĻҶȻ�֮��ʵ��Ҳ����˶�ֵ��
		Mat grayMat=new Mat();
		Mat binaryMat=new Mat();
		if(mat.channels()!=1) {
			Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY);
			Imgproc.adaptiveThreshold(grayMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -10);
		}else {
//			System.out.println("��ͨ��");
			binaryMat=mat;
		}
		//���Ҷ�ͼ���ж�ֵ��
//		Imgproc.adaptiveThreshold(grayMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -10);
//		Imshow.imshow(binaryMat);
		//�����Ļ�Ϊˮƽ+��ֱά�ȵĶ������صĸ���+�ָ�Ϊ4*5�����غ�ÿ������Ķ�������ռ�����صı���
		//ˮƽ����
		int index=0;
		//ͳ�ƶ������ص�����Ŀ
		int white=0;
		//��ֱ����
		for(int row=0; row<binaryMat.rows(); row++) {
			int count=0;
			for(int col=0; col<binaryMat.cols(); col++) {
				//���Ϊ����������ôͳ��
				if((int)binaryMat.get(row, col)[0]==255) {
					white++;
					count++;
				}
			}
			features.put(0, index++, new float[] {(float) count});
		}
		//ˮƽ����
		for(int col=0; col<binaryMat.cols(); col++) {
			int count=0;
			for(int row=0; row<binaryMat.rows(); row++) {
				if((int)binaryMat.get(row, col)[0]==255) {
					count++;
				}
			}
			features.put(0, index++, new float[] {(float) count});
		}

		//�ҵ������Ǹ�����
		double max=0;
		for(int i=0; i<features.cols(); i++) {
			double tmp=features.get(0, i)[0];
			if(tmp>max) {
				max=tmp;
			}
		}
		//���������й�һ��
		for(int i=0; i<features.cols(); i++) {
			double get=features.get(0, i)[0];
			features.put(0, i, new float[]{(float) (get/max)});
		}
		
		
		//��binaryMat����4*5�ֿ飬�ֿ�֮����ÿ���ֿ��ڵĶ����ռ�ܶ����ı���
		for(int i=0; i<5; i++) {
			for(int j=0; j<4; j++) {
				int count=0;
				//������ͳ�Ƹ�����Ķ�����Ŀ
				for(int row=4*i; row<4*i+4; row++) {
					for(int col=5*j; col<5*j+5; col++) {
						if((int) binaryMat.get(row, col)[0]==255) {
							count++;
						}
					}
				}
				features.put(0, index++, new float[] {(float) count/white});
			}
		}
		
//		for(int col=0; col<features.cols(); col++) {
//			System.out.print(features.get(0, col)[0]+"\t");
//		}
//		System.out.println("white: "+white);
		return features;
	}
	
	/**
	 * ʹ�������ɷֵķ�ʽ����ȡ����
	 * @param mat
	 * @return Mat
	 */
	public static Mat extractANNFeature_1(Mat mat) {
		Mat grayMat=new Mat();
		Mat binaryMat=new Mat();
		if(mat.channels()!=1) {
			Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY);	
			Imgproc.adaptiveThreshold(grayMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -10);
		}else {
//			System.out.println("��ͨ��");
			binaryMat=mat;
		}
		Mat mean=new Mat();
		Mat eigenvectors=new Mat();
		Core.PCACompute(binaryMat, 
						mean,
						eigenvectors,
						6);
//		Mat result=new Mat();
//		Core.PCAProject(mat, mean, eigenvectors, result);
		//����������ת��Ϊ1ά��ʽ����
		Mat vectors=new Mat();
		Imgproc.resize(eigenvectors, vectors, new Size(120, 1));
		return vectors;
	}
	
	/**
	 * �������������
	 * @param args
	 */
	public static void main(String []args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String path="E:/ann/0/319_sun_0_21.jpg";
		Mat img=Imgcodecs.imread(path);
		Mat grayMat=new Mat();
		Mat binaryMat=new Mat();
		if(img.channels()!=1) {
			Imgproc.cvtColor(img, grayMat, Imgproc.COLOR_RGB2GRAY);	
			Imgproc.adaptiveThreshold(grayMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -10);
		}else {
//			System.out.println("��ͨ��");
			binaryMat=img;
		}
		Mat res=extractANNFeature_1(binaryMat);
		for(int row=0; row<res.rows(); row++) {
			for(int col=0; col<res.cols(); col++) {
				System.out.print(res.get(row, col)[0]+"\t");
			}
			System.out.println();
		}
	}
	
}
