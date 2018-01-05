package com.xjtu.util;

import org.opencv.core.RotatedRect;

public class VerifySize {
	static final double error=0.9;
	static final double aspect=3.75;
	static final int verifyMin=1;
	static final int verifyMax=120;
	/**
	 * �Դ������ľ��ν��������ֵ��ɸѡ
	 * @param mr
	 * @return boolean
	 */
	public static boolean verifySize(RotatedRect mr) {
		//��������Сֵ
//		int min=34*8*verifyMin;
//		int max=34*8*verifyMax;
		
		int min=44*14*verifyMin;
		int max=44*14*verifyMax;
		//��߱ȵ������Сֵ
		double rmin=aspect*(1-error);
		double rmax=aspect*(1+error);
		//������������
		double s=mr.size.height*mr.size.width;
		double r=mr.size.width/mr.size.height;
		if(r<1) {
			r=mr.size.height/mr.size.width;
		}
//		System.out.println("���֮�ȣ�"+r);
//		System.out.println("�����"+s);
		return (r>=rmin && r<=rmax) && (s>=min && s<=max);
	}

}
