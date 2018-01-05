package com.xjtu.preprocess;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.DeskewAndAffine;
import com.xjtu.util.Imshow;
import com.xjtu.util.Resize;
import com.xjtu.util.VerifySize;

public class SobelLocate {
	//高斯模糊的半径
	private static final int gaussianBlurSize=27; //9
	//调用sobel算子的图像深度的设置
	private static final int ddepth=CvType.CV_16S;
	//图像的放缩比例
	private static final int scale=1;
	private static final int delta=0;
	//调用形态学处理图像时的矩形模板的大小
	private static final int morphSizeWidth=17; //17
	private static final int morphSizeHeight=3; //3
	
	/**
	 * 对输入图像进行灰化，二值化,高斯模糊，形态学闭运算处理
	 * @param original
	 * @param BinarizedMat
	 */
	public static void sobelLocate(Mat original, List<RotatedRect> results) {
		
		Mat grayMat=new Mat();
		//对图像进行灰度化
		Imgproc.cvtColor(original,grayMat, Imgproc.COLOR_RGB2GRAY);
		//使用opencv灰度化函数进行灰化之后，3通道变1通道；单个mat元素大小也变成了1
		//System.out.println("channels:"+grayMat.channels()+","+"mat单个元素的大小："+(int)grayMat.elemSize());
		//对灰度图进行高斯模糊处理
		Size size=new Size(gaussianBlurSize,gaussianBlurSize);
		Mat bluredMat =new Mat();
		Imgproc.GaussianBlur(grayMat, bluredMat, size, 0, 0);
//		Imshow.imshow(bluredMat);
		//sobel边缘检测
		Mat sobelMat=new Mat();
		Mat sobelXMat=new Mat();
		Mat absMat=new Mat();
		Mat absXMat=new Mat();
		Mat dst=new Mat();
		//参数：输入，输出，图像的深度，x导数，y导数，模板大小，后面3个参数都是默认值,在这块只做x方向sobel算子是为了防止水平方向的边缘的影响
		Imgproc.Sobel(bluredMat, sobelMat, ddepth, 1, 0, 3, scale, delta,Core.BORDER_DEFAULT);
		//水平方向的sobel
//		Imgproc.Sobel(bluredMat, sobelXMat, ddepth, 0, 1, 3, scale, delta,Core.BORDER_DEFAULT);
		//这块主要是在计算sobelx方向的导数的时候，结果可能不再0--255范围内，需要对计算之后的结果取绝对值
		Core.convertScaleAbs(sobelMat, absMat);
//		Core.convertScaleAbs(sobelXMat, absXMat);
		//将水平和垂直方向的梯度进行加和
//		Core.addWeighted(absMat, 0.5, absXMat, 0.5, 0, dst);
//		Imshow.imshow(absMat);
		//对图像进行二值化
		Mat binaryMat=new Mat();
		//参数：输入，输出，阈值（使用大津法阈值），最大阈值，类型（背景为0还是1）
		Imgproc.threshold(absMat, binaryMat, Imgproc.THRESH_OTSU, 255,0);
//		Imgproc.adaptiveThreshold(absMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -10);
//		Imshow.imshow(binaryMat);
		//闭运算
		Mat closedMat=new Mat();
		//闭运算模板的大小
		Mat element=Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(morphSizeWidth,morphSizeHeight));
		Imgproc.morphologyEx(binaryMat, closedMat, Imgproc.MORPH_CLOSE, element);
//		Imshow.imshow(closedMat);
		//查找轮廓
		List<MatOfPoint> contours=new ArrayList<MatOfPoint>(); //存放轮廓
		Mat hierarchy=new Mat(); //存放轮廓之间的包含关系，用不到
		Imgproc.findContours(closedMat, contours, hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
		//求轮廓的旋转外接矩形，并对其根据面积阈值进行筛选
		for(int i=0; i<contours.size(); i++) {
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
//		//在二值图像上面绘制轮廓，主要是为了看一下效果
//		for(int i=0; i<results.size(); i++) {
//			Imgproc.drawContours(original, contours, i,new Scalar(0,0,255),1);
//			System.out.println("绘制轮廓");
//		}
//		Imshow.imshow(original);
		System.out.println("可能区域的数量："+results.size());
	}
}
