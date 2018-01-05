package com.xjtu.preprocess;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.RotatedRect;
import org.opencv.features2d.MSER;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;
import com.xjtu.util.VerifySize;

public class MserLocate {
	public static void mserLocate(Mat original, List<RotatedRect> results){	

		Mat image=original;
		List <Mat>match=new ArrayList<Mat>();
		List<Integer>flags=new ArrayList<Integer>();
		final int imageArea=original.rows()*original.cols();
		final int delta=1;
		final int minArea=30;
		final double maxAreaRatio=0.05;
		MSER mser=MSER.create(delta, minArea, (int)(imageArea*maxAreaRatio), 0.25, 0.2, 200, 1.01, 0.003, 5);
		Mat gray=new Mat();
		Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);

		//检测出区域
		List<MatOfPoint> pts=new ArrayList<MatOfPoint>();
		MatOfRect bboxes = new MatOfRect();
		mser.detectRegions(gray, pts, bboxes);
		boolean isClosed=true;
		System.out.println("mser 提取出来区域的个数:"+pts.size());
		//标记这些区域
		//求轮廓的旋转外接矩形，并对其根据面积阈值进行筛选
		for(int i=0; i<pts.size(); i++) {
			//格式转换 matOfpoint-->matOfPoint2f
			MatOfPoint2f m2f=new MatOfPoint2f();
			pts.get(i).convertTo(m2f, CvType.CV_32F);
			//获得轮廓的最小旋转矩形
			RotatedRect mr=Imgproc.minAreaRect(m2f);
			//验证该矩形是否符合面积阈值
			if(VerifySize.verifySize(mr)) {
				
				results.add(mr);
			}
		}
//		//在二值图像上面绘制轮廓，主要是为了看一下效果
//		for(int i=0; i<results.size(); i++) {
//			Imgproc.drawContours(original, pts, i,new Scalar(0,0,255),1);
//		}
		Imshow.imshow(original);
		System.out.println("可能区域的数量："+results.size());
		//倾斜矫正在主函数里面，不再这里调用了额
	}
}
