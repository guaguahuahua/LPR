package com.xjtu.svm;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.Ml;
import org.opencv.ml.SVM;

import com.xjtu.util.Imshow;

public class TestSVM {

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//展示数据
		int width=512,height=512;
		Mat image=Mat.zeros(height, width, CvType.CV_8UC3);
		//设置训练数据
		int []labels= {1, -1, -1, -1};
		float [][]trainningData= {{501, 10}, {255, 10}, {501, 255}, {10, 501}};
		//创建数据mat
		Mat trainningDataMat=new Mat(4, 2, CvType.CV_32FC1);
		for(int row=0; row<trainningDataMat.rows(); row++) {
			for(int col=0; col<trainningDataMat.cols(); col++) {
				trainningDataMat.put(row, col, new float[] { trainningData[row][col]});
			}
		}
		
		//创建标签mat
		Mat labelsMat=new Mat(4, 1, CvType.CV_32SC1);
		for(int row=0, col=0; row<labelsMat.rows(); row++) {
				labelsMat.put(row, col, new int[] {labels[row]});
		}
		
		//训练SVM
		SVM svm=SVM.create();
		svm.setType(SVM.C_SVC);
		svm.setKernel(SVM.LINEAR);
		svm.setTermCriteria(new TermCriteria(TermCriteria.MAX_ITER, 100, 0.000001));
//		svm.train(trainningDataMat, Ml.ROW_SAMPLE, labelsMat);
		svm.trainAuto(trainningDataMat, Ml.ROW_SAMPLE, labelsMat);
		
		//展示svm的决策区域
		for(int i=0; i<image.rows(); i++) {
			for(int j=0; j<image.cols(); j++) {
				Mat sampleMat = new Mat(1, 2, CvType.CV_32FC1);
				sampleMat.put(0, 0, new float[] {j, i});
				sampleMat.put(0, 1, new float[] {j, i});
				float response=svm.predict(sampleMat);
				System.out.println(response);
				if(response==1) {
					image.put(i, j, new byte[] {0, (byte)255, 0});
				}else if(response==-1) {
					image.put(i, j, new byte[] {(byte) 255, 0, 0});
				}
			}
		}

		//在图上面标识训练数据
		int thickness=-1;
		int lineType=8;
		int shift=0;
		Imgproc.circle(image, new Point(501, 10), 5, new Scalar(0,0,0), thickness, lineType, shift);
		Imgproc.circle(image, new Point(255, 10), 5, new Scalar(255,255,255), thickness, lineType, shift);
		Imgproc.circle(image, new Point(501, 255), 5, new Scalar(255,255,255), thickness, lineType, shift);
		Imgproc.circle(image, new Point(10, 501), 5, new Scalar(255,255,255), thickness, lineType, shift);

		//标识支持向量
		int thickness_1=2;
		int lineType_1=8;
		Mat sv=svm.getSupportVectors();
		System.out.println(sv.rows());
		for(int i=0; i<sv.rows(); i++) {
			double []t=sv.get(i, 0);
			System.out.println(t.length);
			Imgproc.circle(image, new Point((int) t[0],(int) t[0]), 6, new Scalar(128, 128, 128), thickness_1, lineType_1, 0);
		}
		Imshow.imshow(image);
	}

}
