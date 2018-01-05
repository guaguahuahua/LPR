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

		//��������
		List<MatOfPoint> pts=new ArrayList<MatOfPoint>();
		MatOfRect bboxes = new MatOfRect();
		mser.detectRegions(gray, pts, bboxes);
		boolean isClosed=true;
		System.out.println("mser ��ȡ��������ĸ���:"+pts.size());
		//�����Щ����
		//����������ת��Ӿ��Σ���������������ֵ����ɸѡ
		for(int i=0; i<pts.size(); i++) {
			//��ʽת�� matOfpoint-->matOfPoint2f
			MatOfPoint2f m2f=new MatOfPoint2f();
			pts.get(i).convertTo(m2f, CvType.CV_32F);
			//�����������С��ת����
			RotatedRect mr=Imgproc.minAreaRect(m2f);
			//��֤�þ����Ƿ���������ֵ
			if(VerifySize.verifySize(mr)) {
				
				results.add(mr);
			}
		}
//		//�ڶ�ֵͼ�����������������Ҫ��Ϊ�˿�һ��Ч��
//		for(int i=0; i<results.size(); i++) {
//			Imgproc.drawContours(original, pts, i,new Scalar(0,0,255),1);
//		}
		Imshow.imshow(original);
		System.out.println("���������������"+results.size());
		//��б���������������棬������������˶�
	}
}
