package com.xjtu.svm;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class ExtractFeature {

	/**
	 * 提取rgbMat中的特征， 以172维度的向量返回
	 * @param rgbMat Mat
	 * 					输入的彩色图像
	 * @return Mat
	 * 					返回抽取原二值图像的特征，以mat的形式返回
	 */
	public static Mat extractFeature(Mat rgbMat) {
		Mat feature=new Mat(1, rgbMat.rows()+rgbMat.cols(), CvType.CV_8UC1);
		//rgb图像 转化为灰度图
		Mat grayMat=new Mat();
		Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_BGR2GRAY);
		//灰度图转二值图
		Mat binaryMat=new Mat();
		Imgproc.adaptiveThreshold(grayMat, binaryMat, 1, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 25, 10);
		//统计水平和垂直方向的1的个数
		//垂直方向
		int i;
		for(i=0; i<binaryMat.rows(); i++) {
			int count=0;
			for(int j=0; j<binaryMat.cols(); j++) {
				count+=binaryMat.get(i, j)[0];
//				System.out.println(binaryMat.get(i, j)[0]);
			}
			feature.put(0, i, new byte[] {(byte)count});
		}
		//水平方向
		for(int col=0; col<binaryMat.cols(); col++) {
			int count=0;
			for(int row=0; row<binaryMat.rows(); row++) {
				count+=binaryMat.get(row, col)[0];
			}
			feature.put(0, i++, count);
		}
		//以mat的形式将抽取的特征返回（水平+垂直方向）
		return feature;
	}
	
	/**
	 * 阶段行测试
	 * @param args
	 */
	public static void main(String []args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String file="E:/HasPlate/plate (1).jpg";
		Mat original=Imgcodecs.imread(file);
		//生成一个特征矩阵
		Mat feature=new Mat(1, original.rows()+original.cols(), CvType.CV_8UC1);
		feature=extractFeature(original);
		//test para 输出feature中的所有元素
		System.out.println("feature.size: "+feature.rows()+","+feature.cols());
		for(int j=0; j<feature.cols(); j++) {
			System.out.print(feature.get(0, j)[0]+"\t");
		}
		System.out.println();
	}
}
