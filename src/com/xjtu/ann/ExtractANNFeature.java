package com.xjtu.ann;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ExtractANNFeature {

	/**
	 * 抽取输入字符的特征,
	 * 注意：输入mat是3通道的
	 * @param mat
	 * @return 
	 */
	public static Mat extractANNFeature(Mat mat) {
		//存放抽取出来的特征
		Mat features=new Mat(1, 60, CvType.CV_32FC1);
		//灰度化,由于原图像本来就是三通的二值图，直接灰度化，变成单通道的二值图，完成了二值化，所以这块的灰度化之后实际也完成了二值化
		Mat grayMat=new Mat();
		Mat binaryMat=new Mat();
		if(mat.channels()!=1) {
			Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY);
			Imgproc.adaptiveThreshold(grayMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -10);
		}else {
//			System.out.println("单通道");
			binaryMat=mat;
		}
		//将灰度图进行二值化
//		Imgproc.adaptiveThreshold(grayMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -10);
//		Imshow.imshow(binaryMat);
		//特征的话为水平+垂直维度的对象像素的个数+分割为4*5的像素后每个区域的对象像素占总像素的比重
		//水平方向
		int index=0;
		//统计对象像素点总数目
		int white=0;
		//垂直方向
		for(int row=0; row<binaryMat.rows(); row++) {
			int count=0;
			for(int col=0; col<binaryMat.cols(); col++) {
				//如果为对象像素那么统计
				if((int)binaryMat.get(row, col)[0]==255) {
					white++;
					count++;
				}
			}
			features.put(0, index++, new float[] {(float) count});
		}
		//水平方向
		for(int col=0; col<binaryMat.cols(); col++) {
			int count=0;
			for(int row=0; row<binaryMat.rows(); row++) {
				if((int)binaryMat.get(row, col)[0]==255) {
					count++;
				}
			}
			features.put(0, index++, new float[] {(float) count});
		}

		//找到最大的那个分量
		double max=0;
		for(int i=0; i<features.cols(); i++) {
			double tmp=features.get(0, i)[0];
			if(tmp>max) {
				max=tmp;
			}
		}
		//对特征进行归一化
		for(int i=0; i<features.cols(); i++) {
			double get=features.get(0, i)[0];
			features.put(0, i, new float[]{(float) (get/max)});
		}
		
		
		//对binaryMat进行4*5分块，分块之后求每个分块内的对象点占总对象点的比重
		for(int i=0; i<5; i++) {
			for(int j=0; j<4; j++) {
				int count=0;
				//遍历，统计该区域的对象数目
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
	 * 使用了主成分的方式来抽取特征
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
//			System.out.println("单通道");
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
		//将特征向量转化为1维形式返回
		Mat vectors=new Mat();
		Imgproc.resize(eigenvectors, vectors, new Size(120, 1));
		return vectors;
	}
	
	/**
	 * 测试上面的特征
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
//			System.out.println("单通道");
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
