package com.xjtu.util;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class DrawBoundingRectangle {
	
	/**
	 * 在给定的original Mat上面， 将动态数组中的RotatedRect的轮廓绘制出来
	 * @param original Mat
	 * 					输入图像 
	 * @param results List<RotatedRect> 
	 * 					存放RotatedRect 的动态数组
	 */
	public static void drawBoundingRectangle(Mat original, List<RotatedRect>results) {
		
		Point[]p=new Point[4];
		for(int i=0; i<results.size(); i++) {
			RotatedRect mr=results.get(i);
			mr.points(p);
			for(int index=0; index<4; index++) {
				Point p1=new Point(p[index].x,p[index].y);
				Point p2=new Point(p[(index+1)%4].x,p[(index+1)%4].y) ;
				Imgproc.line(original, p1, p2, new Scalar(255,255,0),4);
			}
		}		
	}
	
	/**
	 * 在给定图像上面将矩形的轮廓绘制出来
	 * @param noRivetMat Mat
	 * 						输入图像
	 * @param rectangles List<Rect>
	 * 						矩形轮廓的存放集合
	 */
	public static void drawBoundingRectangle_1(Mat noRivetMat, List<Rect> rectangles) {
		Point[]p=new Point[4];
		for(int i=0; i<p.length; i++) {
			p[i]=new Point();
		}
		for(int i=0; i<rectangles.size(); i++) {
			Rect mr=rectangles.get(i);
			p[0].set(new double[] {mr.x, mr.y});
			p[1].set(new double[] {mr.x+mr.width, mr.y});
			p[2].set(new double[] {mr.x+mr.width, mr.y+mr.height});
			p[3].set(new double[] {mr.x, mr.y+mr.height});
			for(int index=0; index<4; index++) {
				Point p1=new Point(p[index].x,p[index].y);
				Point p2=new Point(p[(index+1)%4].x,p[(index+1)%4].y) ;
				Imgproc.line(noRivetMat, p1, p2, new Scalar(255,255,255),2);
			}
		}
	}
}
