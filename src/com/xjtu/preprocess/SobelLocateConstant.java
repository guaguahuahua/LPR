package com.xjtu.preprocess;

import org.opencv.core.CvType;

public final class SobelLocateConstant {
	//��˹ģ���İ뾶
	private static final int gaussianBlurSize=27; //9
	//����sobel���ӵ�ͼ����ȵ�����
	private static final int ddepth=CvType.CV_16S;
	//ͼ��ķ�������
	private static final int scale=1;
	private static final int delta=0;
	//������̬ѧ����ͼ��ʱ�ľ���ģ��Ĵ�С
	private static final int morphSizeWidth=17; //17
	private static final int morphSizeHeight=3; //3
	
	public static int getGaussianblursize() {
		return gaussianBlurSize;
	}
	public static int getDdepth() {
		return ddepth;
	}
	public static int getScale() {
		return scale;
	}
	public static int getDelta() {
		return delta;
	}
	public static int getMorphsizewidth() {
		return morphSizeWidth;
	}
	public static int getMorphsizeheight() {
		return morphSizeHeight;
	}
}
