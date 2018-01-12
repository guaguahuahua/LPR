package com.xjtu.preprocess;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.DeskewAndAffine;
import com.xjtu.util.Imshow;
import com.xjtu.util.VerifySize;

public class ColorLocate {
	
	/**
	 * 颜色定位,使用了自适应颜色阈值
	 * @param original Mat
	 * 					输入图像
	 * @param color int 
	 * 					进行定位的颜色
	 * @param results RotatedRect
	 * 					可能为车牌轮廓的旋转矩形
	 * @param debug boolean
	 * 					是否进入调试模式（显示图像处理的细节）
	 */
	public static void colorLocate(Mat original, int color, List<RotatedRect> results, boolean debug) {
		//设定s，v分量值的变化范围
		int maxSV=255;
		int minSV=95;
		int minRefSV=64;
		//蓝色色度范围
		int minBlue=100;
		int maxBlue=140; //120
//		//黄色色度范围
//		int minYellow=15;
//		int maxYellow=40;
		//图像转到hsv空间
		Mat hsvMat=new Mat();
		Imgproc.cvtColor(original, hsvMat, Imgproc.COLOR_BGR2HSV);
		if(debug) {
			Imshow.imshow(hsvMat, "HSV空间的图像.ColorLocate");			
		}
		//对亮度v进行直方图均衡化
		List<Mat> components=new ArrayList<Mat>();
		Core.split(hsvMat, components);
		Imgproc.equalizeHist(components.get(2), components.get(2));
		//将均衡之后的图像进行合并
		Core.merge(components, hsvMat);
		if(debug) {
			Imshow.imshow(hsvMat, "V分量直方图均衡化后的图像.ColorLocate");
		}
		int maxH=0;
		int minH=0;
		//判断对哪种颜色的车牌进行识别
		switch(color) {
		
		//蓝色车牌
		case 1:
		maxH=maxBlue;
		minH=minBlue;
		break;

		//其他颜色
		default:
			System.out.print("ColorLocate: 无法识别该颜色");
			break;
		}
		//色度值的范围的一半
		double diffH=(maxH-minH)/2;
		//色度的均值
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
					//System.out.println("r: "+r);
					//设定SV的最小阈值
					minSV=(int) (minRefSV-minRefSV/2*(1-r));
					//判断是否满足sv值
					if(s>=minSV && s<=maxSV && v>=minSV && v<=maxSV) {
						match=true;
					}
				}
				//满足阈值者置为对象，否则置为背景
				if(match) {
					hsvMat.put(row, col, new byte[] {(byte)0, (byte)0, (byte)255});
				}else {
					hsvMat.put(row, col, new byte[] {(byte)0, (byte)0, (byte)0});
				}
			}
		}
		List<Mat> mv=new ArrayList<Mat>();
		//对hsvmat进行分离，得到各个分量，此时它的v分量就是一个二值图
		Core.split(hsvMat, mv);
		//得到v分量的二值图
		Mat grayMat=mv.get(2);
		if(debug) {
			Imshow.imshow(grayMat, "V分量的二值图像.ColorLocate");
		}
		//闭运算，修补空隙
		Mat closedMat=new Mat();
		Mat element=Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(ColorLocateConstant.getMorphsizewidth(), ColorLocateConstant.getMorphsizeheight()));
		Imgproc.morphologyEx(grayMat, closedMat, Imgproc.MORPH_CLOSE, element);
		if(debug) {
			Imshow.imshow(closedMat, "闭运算之后的图像.ColorLocate");
		}
		//查找轮廓
		List<MatOfPoint> contours=new ArrayList<MatOfPoint>(); //存放轮廓
		Mat hierarchy=new Mat(); //存放轮廓之间的包含关系
		Imgproc.findContours(closedMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		System.out.println("ColorLocate part:");
		System.out.println("颜色定位轮廓数量："+contours.size());
		//求轮廓的旋转外接矩形，并对其根据面积阈值进行筛选
		for(int i=0; i<contours.size(); i++) {
			//在原图上面将定位得到的轮廓绘制出来
			if(debug) {
				Imgproc.drawContours(original, contours, i,new Scalar(0,0,255),1);				
			}
			//格式转换 matOfpoint-->matOfPoint2f
			MatOfPoint2f m2f=new MatOfPoint2f();
			contours.get(i).convertTo(m2f, CvType.CV_32F);
			//获得轮廓的最小旋转矩形
			RotatedRect mr=Imgproc.minAreaRect(m2f);
			//验证该矩形是否符合面积阈值
			if(VerifySize.verifySize(mr)) {
				results.add(mr);
			}
		}
		System.out.println("ColorLocate:"+results.size());
	}
}
