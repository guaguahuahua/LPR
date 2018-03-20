package com.xjtu.ann;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class CharFeatures_1 {
	
	/**
	 * 使用easyPR源码中给定的算法进行特征提取
	 * @param mat 原图像的灰度图
	 * @return Mat
	 */
	public static Mat charFeatures_1(Mat mat) {
		if(mat.channels()!=1) {
			Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
		}
//		Imshow.imshow(mat);
		//返回的特征
		Mat features=new Mat(1, 120, CvType.CV_32FC1);
		//获得裁剪区域
		Rect rect=getRect(mat);
		//在这里添加判断，因为在只有一行或者是一列的情形下，会导致裁剪区域大于原区域，从而出现越界错误
		if(rect.width>mat.size().width || rect.height>mat.size().height) {
			rect.width=(int) mat.size().width;
			rect.height=(int) mat.size().height;
		}
//		System.out.println(rect.x+", "+rect.y+", "+rect.height+", "+rect.width);
		Mat area=cutRect(rect, mat);
		//对裁剪区域进行大小归一化10*10
		Imgproc.resize(area, area, new Size(10, 10));
		//水平，垂直方向进行投影
		double []vertical  =project(area, 0);
		double []horizontal=project(area, 1);
		//将水平垂直特征写入到特征数组中去
		int index=0;
		for(int i=0; i<vertical.length; i++) {
			features.put(0, index++, new float[] {(float) vertical[i]});
		}
		for(int i=0; i<horizontal.length; i++) {
			features.put(0, index++, new float[] {(float) horizontal[i]});
		}
		//将低像素的原图直接添加进特征向量中去
		for(int row=0; row<area.rows(); row++) {
			for(int col=0; col<area.cols(); col++) {
				features.put(0, index++, new float[] {(float) area.get(row, col)[0]});
			}
		}
		return features;
	}
	
	/**
	 * 获取字符的类似外接矩形
	 * @param mat 灰度图
	 * @return 矩形
	 */
	private static Rect getRect(Mat mat) {
		
		//确定矩形的上下边界
		int top=0;
		int bottom=mat.rows();
		boolean flag=false;
		for(int row=0; row<mat.rows(); row++) {
			for(int col=0; col<mat.cols(); col++) {
				if(mat.get(row, col)[0]>20) {
					top=row;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		flag=false;
		//下边界
		for(int row=mat.rows()-1; row>-1; row--) {
			for(int col=0; col<mat.cols(); col++) {
				if(mat.get(row, col)[0]>20) {
					bottom=row;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		flag=false;
		//左右边界
		int left=0;
		int right=mat.cols();
		for(int col=0; col<mat.cols(); col++) {
			for(int row=0; row<mat.rows(); row++) {
				if(mat.get(row, col)[0]>20) {
					left=col;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		flag=false;
		//右边界
		for(int col=mat.cols()-1; col>-1; col--) {
			for(int row=0; row<mat.rows(); row++) {
				if(mat.get(row, col)[0]>20) {
					right=col;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		
		//获得矩形参数
		int x=left;
		int y=top;
		int width=right-left+1;
		int height=bottom-top+1;
		Rect rect=new Rect(x, y, width, height);
		return rect;
	}
	
	/**
	 * 将字符区域从原mat上面裁剪下来
	 * @param rect 要裁剪的区域
	 * @param mat 原图
	 * @return Mat
	 */
	private static Mat cutRect(Rect rect, Mat mat) {
		//创建一个返回的mat,将裁剪区域的图像依次复制过来
		Mat cutted=new Mat(mat, rect);	
		return cutted;
	}
	
	/**
	 * 按照方向投影，水平方向为1，垂直方向为0
	 * @param mat
	 * @param direction
	 * @return
	 */
	public static double[] project(Mat mat, int direction) {
		//记录特征,因为图像是20*20 的正方形，所以使用行或者列规定特征数量都可以
		double []features=new double[mat.rows()];
		//记录特征存放的位置
		int index=0;
		//存放遍历结束点的变量
		int end=0;
		if(direction==0) {
			end=mat.cols(); //水平方向
		}else {
			end=mat.rows(); //垂直方向
		}
		int max=0;
		//按照方向投影
		for(int i=0; i<end; i++) {
			Mat data= (direction==0) ? mat.col(i) : mat.row(i);
			int featI=countObject(data);
			//取水平或者垂直方向的投影中的最大值
			max=featI > max ? featI : max; 
			features[index++]=featI;
		}		
		//规格化
		if(max>0) {
			for(int i=0; i<features.length; i++) {
				features[i]=features[i] / max;
			}
		}
		return features;
	}
	
	/**
	 * 计算一行或者是一列中非背景元素的个数
	 * @param data 一行或者是一列元素
	 * @return int
	 */
	public static int countObject(Mat data) {
		int count=0;
		if(data.cols()==1) { //返回了一列数据
			//遍历这一列数如果不是背景，那么count++
			for(int i=0; i<data.rows(); i++) {
				if(data.get(i, 0)[0]!=0) {
					count++;
				}
			}
		}else if(data.rows()==1){
			//遍历一行，非背景元count++
			for(int i=0; i<data.cols(); i++) {
				if(data.get(0, i)[0]!=0) {
					count++;
				}
			}
		}		
		return count;
	}
	
	
	
	public static void main(String []args) {
		System.load("C:\\opencv-3.3.0\\build\\java\\x64\\opencv_java330.dll");
		//处理流程图像的位置
		String file = "E:\\ann\\chars\\2\\201-2.jpg";
		Mat img=Imgcodecs.imread(file);
//		Mat grayImg=new Mat();
//		Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_RGB2GRAY);
		Imshow.imshow(img);
		Mat cutted=charFeatures_1(img);		
	}
}
