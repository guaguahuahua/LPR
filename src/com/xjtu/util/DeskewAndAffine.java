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
	 * ��ͨ�������ֵɸѡ�ľ��������ٴν���һ��ɸѡ����Ҫ��ͨ����б�Ƕ�
	 * ɸ����Щ��ǹ���ľ������򣬲�������б�����������б������Ȼ��ʹ��
	 * ����任����ת����ת��������ˮƽ��
	 * @param src Mat
	 * 				����ͼ��
	 * @param pengdingArea List<RotatedRect>
	 * 				��У�������ת���εļ���		
	 * @param res List<Mat>
	 * 				��Ųü���У�������ͼ��ļ���
	 */
	public static void deskewAndAffine(Mat src, List<RotatedRect>pengdingArea, List<Mat>res) {
		//�Ƕȵķ�Χ
		double minAngle=-60.0;
		double maxAngle= 60.0;
		double angle=0;
		double r=0;
		RotatedRect rr;
		//������������
		for(int i=0; i<pengdingArea.size(); i++) {
			rr=pengdingArea.get(i);
			angle=rr.angle;
			System.out.println(angle);
			//�жϽǶȣ�������ٷ�Χ�ڣ�ֱ�Ӷ�����������һ��������ж�
			if(angle<minAngle && angle>maxAngle) {
				continue;				
			}
			//�������б��
			if(angle!=0) {
				//����Ƕ�û���⣬����һ����ת�任����������ת��
				r=rr.size.width/rr.size.height;
				System.out.println("(width,height)"+rr.size.width+","+rr.size.height);
				//�����С�ڸߣ�����б��
				if(r<1) {
					angle+=90;
					double tmp=rr.size.width;
					rr.size.width=rr.size.height;
					rr.size.height=tmp;
				}
				//���ĽǶ���ʱ����ת�����ĽǶ�˳ʱ����ת
				Mat matrix=Imgproc.getRotationMatrix2D(rr.center, angle, 1);
				//��Ž�������ͼ��
				Mat pengding=new Mat();
				//����任����ĳ�㿪ʼ������ͼ����б任������ٽ��вü�
				Imgproc.warpAffine(src, pengding, matrix, src.size());
				//
				//Imshow.imshow(pengding);
				//�ü�
				//�����Ҫ�ǽ�width<height�������δ���һ�£����򾭹���ת��ü����γ���
				double x;
				double y;
				x=rr.center.x-0.5*rr.size.width;
				y=rr.center.y-0.5*rr.size.height;
				
				//�����ü�����
				if(x<0) {
					x=0;
				}
				if(y<0) {
					y=0;
				}
				//����Ƿ�ֹ�ü�����Խ�磬����ͼ��Ĳ��֣�ֱ������
				if(x+rr.size.width>src.cols()) {
					rr.size.width=src.cols()-x-1;
				}
				if(y+rr.size.height>src.rows()) {
					rr.size.height=src.rows()-y-1;
				}
				System.out.println("x:"+x+","+"y:"+y);
				System.out.println(x+rr.size.width+","+y+rr.size.height);
				//�ü�����ľ���
				Rect rect=new Rect((int) x,(int) y, (int) rr.size.width, (int) rr.size.height);
				//�ü�
				Mat mat=new Mat(pengding,rect);
				Mat t=Resize.resize(mat);
				System.out.println(t.rows()+","+t.cols());
				//���ü������ӵ�������
				res.add(t);
//				Imshow.imshow(t);
			//ͼ��û����б
			}else {
				double x=rr.center.x-0.5*rr.size.width;
				double y=rr.center.y-0.5*rr.size.height;
				if(x<0) {
					x=0;
				}
				if(y<0) {
					y=0;
				}
				//�����ü�����
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
