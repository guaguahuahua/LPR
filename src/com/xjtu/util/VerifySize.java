package com.xjtu.util;

import org.opencv.core.RotatedRect;

public class VerifySize {
	private static final double error=0.9;  //���������ķ�Χ
	private static final double aspect=3.75;//�����   
	private static final int verifyMin=1;   //�����С����
	private static final int verifyMax=120; //���ı��ʣ�����Ǿ���ʵ������õ��Ľ����������Ҫ����
	
	/**
	 * �Դ������ľ��ν��������ֵ��ɸѡ
	 * @param mr RotatedRect 
	 * 				����Ϊ���Ƶ�����
	 * @return boolean
	 *     			�Ƿ�Ϊ�����������ǣ�true������false��
	 */
	public static boolean verifySize(RotatedRect mr) {
		//��������Сֵ
		//�ҹ����ƵĴ�С�ǳ�Ϊ44cm���߶�Ϊ14cm������֤�Ƿ�Ϊ����ͼ���ʱ��
		//��Ҫ��������ε��������Ϊһ��ֵ��������ɸѡ
		int min=44*14*verifyMin; //��С�����
		int max=44*14*verifyMax; //�������
		//��߱ȵ������Сֵ
		double rmin=aspect*(1-error);
		double rmax=aspect*(1+error);
		//������������
		double s=mr.size.height*mr.size.width;
		//������εĳ����
		double r=mr.size.width/mr.size.height;
		//�Գ���Ƚ���һ������������ʱ��������ת��90�ȵ�����
		if(r<1) {
			r=mr.size.height/mr.size.width;
		}
		//����þ���������������ֵ��Χ�ڣ������Ҳ�ڷ�Χ�ڣ�����true�����򷵻�false
		return (r>=rmin && r<=rmax) && (s>=min && s<=max);
	}

}
