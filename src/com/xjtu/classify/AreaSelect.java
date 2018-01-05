package com.xjtu.classify;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class AreaSelect {

	/**
	 * 通过调用colorSelect得到了该图像对应的蓝色面积，现在取面积值最大的那个图像作为最终识别的图像
	 * @param resImg
	 * @param finalImg
	 */
	public static Mat areaSelect(List<Mat> resImg) {
		
		//这是在没有定位到车牌或者是svm判定之后没有车牌时的情形
		if(resImg.size()==0) {
			return null;
		}
		int index=0;
		int maxArea=Integer.MIN_VALUE;
		//遍历所有车牌的图像，得到他们蓝色区域的面积
		for(int i=0; i<resImg.size(); i++) {
			//得到i个图像的蓝色区域面积
			int s=colorSelect(resImg.get(i));
			//得到了最大的面积值和对应的位置
			if(s>maxArea) {
				maxArea=s;
				index=i;
			}
		}
		//返回蓝色区域面积最大的那张图像
		Mat finalImg=resImg.get(index);
		return finalImg;
	}
	
	/**
	 * 从给定的图像中将蓝色区域置为对象其他颜色置为背景，并返回蓝色区域的面积
	 * @param mat
	 * @return
	 */
	public static int colorSelect(Mat original) {
		//设置s，v的变化区间
		int maxSV=255;
		int minSV=95;
		int minRefSV=64;
		//蓝色色度范围
		int minBlue=100;
		int maxBlue=140; //120 
		//图像转到hsv空间
		Mat hsvMat=new Mat();
		Imgproc.cvtColor(original, hsvMat, Imgproc.COLOR_BGR2HSV);
		//对亮度v进行直方图均衡化
		List<Mat> components=new ArrayList<Mat>();
		Core.split(hsvMat, components);
		Imgproc.equalizeHist(components.get(2), components.get(2));
		//将均衡之后的图像进行合并
		Core.merge(components, hsvMat);
		//设置H变化区间
		int maxH=0;
		int minH=0;
		maxH=maxBlue;
		minH=minBlue;
		
		//色度值的范围的一半（为自适应颜色阈值做准备）
		double diffH=(maxH-minH)/2;
		//色度的均值 （为自适应颜色阈值做准备）
		double averH=minH+diffH;
		//遍历整张图像
		for(int row=0; row<hsvMat.rows(); row++) {
			for(int col=0; col<hsvMat.cols(); col++) {
				double []values=hsvMat.get(row, col);
				int h=(int)values[0];
				int s=(int)values[1];
				int v=(int)values[2];
				
				boolean match=false;
				//判断是否满足色度值
				if(h<=maxH && h>=minH) {
					//计算当前色度和色度均值的差
					double HDiff=Math.abs(h-averH);
					//计算差值在色度范围的比例
					double r=HDiff/diffH;
					//设定SV的最小阈值
//					minSV=(int) (minRefSV-minRefSV/2*(1-r));
					//判断是否满足sv值
					if(s>=minSV && s<=maxSV && v>=minSV && v<=maxSV) {
						match=true;
					}
				}
				//满足阈值者置为对象，否则置为背景
				if(match) {
					hsvMat.put(row, col, new byte[] {(byte)0,(byte)0,(byte)255});
				}else {
					hsvMat.put(row, col, new byte[] {(byte)0,(byte)0,(byte)0});
				}
			}
		}
		List<Mat> mv=new ArrayList<Mat>();
		//对hsvmat进行分离，得到各个分量，此时它的v分量就是一个二值图
		Core.split(hsvMat, mv);
		//得到v分量的二值图
		Mat binaryMat=mv.get(2);
		//记录白色像素值的变量
		int count=0;
		//统计图像中对象点（255）点的数量
		for(int row=0; row<binaryMat.rows(); row++) {
			for(int col=0; col<binaryMat.cols(); col++) {
				double []pixels=binaryMat.get(row, col);
				//如果是255，count++
				if((int) pixels[0]==255) {
					count++;
				}
			}
		}
		return count;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
