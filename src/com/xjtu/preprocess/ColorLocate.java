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
	 * ��ɫ��λ,ʹ��������Ӧ��ɫ��ֵ
	 * @param original Mat
	 * 					����ͼ��
	 * @param color int 
	 * 					���ж�λ����ɫ
	 * @param results RotatedRect
	 * 					����Ϊ������������ת����
	 * @param debug boolean
	 * 					�Ƿ�������ģʽ����ʾͼ�����ϸ�ڣ�
	 */
	public static void colorLocate(Mat original, int color, List<RotatedRect> results, boolean debug) {
		//�趨s��v����ֵ�ı仯��Χ
		int maxSV=255;
		int minSV=95;
		int minRefSV=64;
		//��ɫɫ�ȷ�Χ
		int minBlue=100;
		int maxBlue=140; //120
//		//��ɫɫ�ȷ�Χ
//		int minYellow=15;
//		int maxYellow=40;
		//ͼ��ת��hsv�ռ�
		Mat hsvMat=new Mat();
		Imgproc.cvtColor(original, hsvMat, Imgproc.COLOR_BGR2HSV);
		if(debug) {
			Imshow.imshow(hsvMat, "HSV�ռ��ͼ��.ColorLocate");			
		}
		//������v����ֱ��ͼ���⻯
		List<Mat> components=new ArrayList<Mat>();
		Core.split(hsvMat, components);
		Imgproc.equalizeHist(components.get(2), components.get(2));
		//������֮���ͼ����кϲ�
		Core.merge(components, hsvMat);
		if(debug) {
			Imshow.imshow(hsvMat, "V����ֱ��ͼ���⻯���ͼ��.ColorLocate");
		}
		int maxH=0;
		int minH=0;
		//�ж϶�������ɫ�ĳ��ƽ���ʶ��
		switch(color) {
		
		//��ɫ����
		case 1:
		maxH=maxBlue;
		minH=minBlue;
		break;

		//������ɫ
		default:
			System.out.print("ColorLocate: �޷�ʶ�����ɫ");
			break;
		}
		//ɫ��ֵ�ķ�Χ��һ��
		double diffH=(maxH-minH)/2;
		//ɫ�ȵľ�ֵ
		double averH=minH+diffH;
		//��������ͼ��
		for(int row=0; row<hsvMat.rows(); row++) {
			for(int col=0; col<hsvMat.cols(); col++) {
				double []values=hsvMat.get(row, col);
				int h=(int)values[0];
				int s=(int)values[1];
				int v=(int)values[2];
				
				boolean match=false;
				//�ж��Ƿ�����ɫ��ֵ
				if(h<=maxH && h>=minH) {
					//���㵱ǰɫ�Ⱥ�ɫ�Ⱦ�ֵ�Ĳ�
					double HDiff=Math.abs(h-averH);
					//�����ֵ��ɫ�ȷ�Χ�ı���
					double r=HDiff/diffH;
					//System.out.println("r: "+r);
					//�趨SV����С��ֵ
					minSV=(int) (minRefSV-minRefSV/2*(1-r));
					//�ж��Ƿ�����svֵ
					if(s>=minSV && s<=maxSV && v>=minSV && v<=maxSV) {
						match=true;
					}
				}
				//������ֵ����Ϊ���󣬷�����Ϊ����
				if(match) {
					hsvMat.put(row, col, new byte[] {(byte)0, (byte)0, (byte)255});
				}else {
					hsvMat.put(row, col, new byte[] {(byte)0, (byte)0, (byte)0});
				}
			}
		}
		List<Mat> mv=new ArrayList<Mat>();
		//��hsvmat���з��룬�õ�������������ʱ����v��������һ����ֵͼ
		Core.split(hsvMat, mv);
		//�õ�v�����Ķ�ֵͼ
		Mat grayMat=mv.get(2);
		if(debug) {
			Imshow.imshow(grayMat, "V�����Ķ�ֵͼ��.ColorLocate");
		}
		//�����㣬�޲���϶
		Mat closedMat=new Mat();
		Mat element=Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(ColorLocateConstant.getMorphsizewidth(), ColorLocateConstant.getMorphsizeheight()));
		Imgproc.morphologyEx(grayMat, closedMat, Imgproc.MORPH_CLOSE, element);
		if(debug) {
			Imshow.imshow(closedMat, "������֮���ͼ��.ColorLocate");
		}
		//��������
		List<MatOfPoint> contours=new ArrayList<MatOfPoint>(); //�������
		Mat hierarchy=new Mat(); //�������֮��İ�����ϵ
		Imgproc.findContours(closedMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		System.out.println("ColorLocate part:");
		System.out.println("��ɫ��λ����������"+contours.size());
		//����������ת��Ӿ��Σ���������������ֵ����ɸѡ
		for(int i=0; i<contours.size(); i++) {
			//��ԭͼ���潫��λ�õ����������Ƴ���
			if(debug) {
				Imgproc.drawContours(original, contours, i,new Scalar(0,0,255),1);				
			}
			//��ʽת�� matOfpoint-->matOfPoint2f
			MatOfPoint2f m2f=new MatOfPoint2f();
			contours.get(i).convertTo(m2f, CvType.CV_32F);
			//�����������С��ת����
			RotatedRect mr=Imgproc.minAreaRect(m2f);
			//��֤�þ����Ƿ���������ֵ
			if(VerifySize.verifySize(mr)) {
				results.add(mr);
			}
		}
		System.out.println("ColorLocate:"+results.size());
	}
}
