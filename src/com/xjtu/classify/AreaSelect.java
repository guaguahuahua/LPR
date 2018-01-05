package com.xjtu.classify;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class AreaSelect {

	/**
	 * ͨ������colorSelect�õ��˸�ͼ���Ӧ����ɫ���������ȡ���ֵ�����Ǹ�ͼ����Ϊ����ʶ���ͼ��
	 * @param resImg
	 * @param finalImg
	 */
	public static Mat areaSelect(List<Mat> resImg) {
		
		//������û�ж�λ�����ƻ�����svm�ж�֮��û�г���ʱ������
		if(resImg.size()==0) {
			return null;
		}
		int index=0;
		int maxArea=Integer.MIN_VALUE;
		//�������г��Ƶ�ͼ�񣬵õ�������ɫ��������
		for(int i=0; i<resImg.size(); i++) {
			//�õ�i��ͼ�����ɫ�������
			int s=colorSelect(resImg.get(i));
			//�õ����������ֵ�Ͷ�Ӧ��λ��
			if(s>maxArea) {
				maxArea=s;
				index=i;
			}
		}
		//������ɫ���������������ͼ��
		Mat finalImg=resImg.get(index);
		return finalImg;
	}
	
	/**
	 * �Ӹ�����ͼ���н���ɫ������Ϊ����������ɫ��Ϊ��������������ɫ��������
	 * @param mat
	 * @return
	 */
	public static int colorSelect(Mat original) {
		//����s��v�ı仯����
		int maxSV=255;
		int minSV=95;
		int minRefSV=64;
		//��ɫɫ�ȷ�Χ
		int minBlue=100;
		int maxBlue=140; //120 
		//ͼ��ת��hsv�ռ�
		Mat hsvMat=new Mat();
		Imgproc.cvtColor(original, hsvMat, Imgproc.COLOR_BGR2HSV);
		//������v����ֱ��ͼ���⻯
		List<Mat> components=new ArrayList<Mat>();
		Core.split(hsvMat, components);
		Imgproc.equalizeHist(components.get(2), components.get(2));
		//������֮���ͼ����кϲ�
		Core.merge(components, hsvMat);
		//����H�仯����
		int maxH=0;
		int minH=0;
		maxH=maxBlue;
		minH=minBlue;
		
		//ɫ��ֵ�ķ�Χ��һ�루Ϊ����Ӧ��ɫ��ֵ��׼����
		double diffH=(maxH-minH)/2;
		//ɫ�ȵľ�ֵ ��Ϊ����Ӧ��ɫ��ֵ��׼����
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
					//�趨SV����С��ֵ
//					minSV=(int) (minRefSV-minRefSV/2*(1-r));
					//�ж��Ƿ�����svֵ
					if(s>=minSV && s<=maxSV && v>=minSV && v<=maxSV) {
						match=true;
					}
				}
				//������ֵ����Ϊ���󣬷�����Ϊ����
				if(match) {
					hsvMat.put(row, col, new byte[] {(byte)0,(byte)0,(byte)255});
				}else {
					hsvMat.put(row, col, new byte[] {(byte)0,(byte)0,(byte)0});
				}
			}
		}
		List<Mat> mv=new ArrayList<Mat>();
		//��hsvmat���з��룬�õ�������������ʱ����v��������һ����ֵͼ
		Core.split(hsvMat, mv);
		//�õ�v�����Ķ�ֵͼ
		Mat binaryMat=mv.get(2);
		//��¼��ɫ����ֵ�ı���
		int count=0;
		//ͳ��ͼ���ж���㣨255���������
		for(int row=0; row<binaryMat.rows(); row++) {
			for(int col=0; col<binaryMat.cols(); col++) {
				double []pixels=binaryMat.get(row, col);
				//�����255��count++
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
