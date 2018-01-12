package com.xjtu.util;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class DeskewAndAffine {

	/**
	 * 对通过面积阈值筛选的矩形区域再次进行一个筛选，主要是通过倾斜角度
	 * 筛掉那些倾角过大的矩形区域，并对有倾斜的区域进行倾斜矫正，然后使用
	 * 仿射变换将旋转矩形转正（保持水平）
	 * @param src Mat
	 * 				输入图像
	 * @param pengdingArea List<RotatedRect>
	 * 				待校正存放旋转矩形的集合		
	 * @param res List<Mat>
	 * 				存放裁剪的校正过后的图像的集合
	 */
	public static void deskewAndAffine(Mat src, List<RotatedRect>pengdingArea, List<Mat>res) {
		//角度的范围
		double minAngle=-60.0;
		double maxAngle= 60.0;
		double angle=0;
		double r=0;
		RotatedRect rr;
		//遍历所有区域
		for(int i=0; i<pengdingArea.size(); i++) {
			rr=pengdingArea.get(i);
			angle=rr.angle;
			System.out.println(angle);
			//判断角度，如果不再范围内，直接丢弃，进行下一个区域的判定
			if(angle<minAngle && angle>maxAngle) {
				continue;				
			}
			//如果有倾斜角
			if(angle!=0) {
				//如果角度没问题，进行一个旋转变换将矩形区域转正
				r=rr.size.width/rr.size.height;
				System.out.println("(width,height)"+rr.size.width+","+rr.size.height);
				//如果宽小于高，左倾斜，
				if(r<1) {
					angle+=90;
					double tmp=rr.size.width;
					rr.size.width=rr.size.height;
					rr.size.height=tmp;
				}
				//正的角度逆时针旋转，负的角度顺时针旋转
				Mat matrix=Imgproc.getRotationMatrix2D(rr.center, angle, 1);
				//存放矫正过的图像
				Mat pengding=new Mat();
				//仿射变换，从某点开始对整个图像进行变换，最后再进行裁剪
				Imgproc.warpAffine(src, pengding, matrix, src.size());
				//
				//Imshow.imshow(pengding);
				//裁剪
				//这块主要是将width<height这种情形处理一下，否则经过旋转后裁剪矩形出错
				double x;
				double y;
				x=rr.center.x-0.5*rr.size.width;
				y=rr.center.y-0.5*rr.size.height;
				
				//创建裁剪区域
				if(x<0) {
					x=0;
				}
				if(y<0) {
					y=0;
				}
				//这块是防止裁剪区域越界，超过图像的部分，直接抛弃
				if(x+rr.size.width>src.cols()) {
					rr.size.width=src.cols()-x-1;
				}
				if(y+rr.size.height>src.rows()) {
					rr.size.height=src.rows()-y-1;
				}
				System.out.println("x:"+x+","+"y:"+y);
				System.out.println(x+rr.size.width+","+y+rr.size.height);
				//裁剪区域的矩形
				Rect rect=new Rect((int) x,(int) y, (int) rr.size.width, (int) rr.size.height);
				//裁剪
				Mat mat=new Mat(pengding,rect);
				Mat t=Resize.resize(mat);
				System.out.println(t.rows()+","+t.cols());
				//将裁剪结果添加到集合中
				res.add(t);
//				Imshow.imshow(t);
			//图像没有倾斜
			}else {
				double x=rr.center.x-0.5*rr.size.width;
				double y=rr.center.y-0.5*rr.size.height;
				if(x<0) {
					x=0;
				}
				if(y<0) {
					y=0;
				}
				//创建裁剪区域
				Rect rect=new Rect((int) x,(int) y, (int) rr.size.width, (int) rr.size.height);
				Mat mat=new Mat(src,rect);
				Mat t=Resize.resize(mat);
				System.out.println(t.rows()+","+t.cols());
				res.add(t);
//				Imshow.imshow(t);
			}
		}
	}
}
