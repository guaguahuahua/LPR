package com.xjtu.preprocess;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.DeskewAndAffine;
import com.xjtu.util.Imshow;
import com.xjtu.util.Resize;
import com.xjtu.util.VerifySize;

public class SobelLocate {
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
	
	/**
	 * ������ͼ����лһ�����ֵ��,��˹ģ������̬ѧ�����㴦��
	 * @param original
	 * @param BinarizedMat
	 */
	public static void sobelLocate(Mat original, List<RotatedRect> results) {
		
		Mat grayMat=new Mat();
		//��ͼ����лҶȻ�
		Imgproc.cvtColor(original,grayMat, Imgproc.COLOR_RGB2GRAY);
		//ʹ��opencv�ҶȻ��������лһ�֮��3ͨ����1ͨ��������matԪ�ش�СҲ�����1
		//System.out.println("channels:"+grayMat.channels()+","+"mat����Ԫ�صĴ�С��"+(int)grayMat.elemSize());
		//�ԻҶ�ͼ���и�˹ģ������
		Size size=new Size(gaussianBlurSize,gaussianBlurSize);
		Mat bluredMat =new Mat();
		Imgproc.GaussianBlur(grayMat, bluredMat, size, 0, 0);
//		Imshow.imshow(bluredMat);
		//sobel��Ե���
		Mat sobelMat=new Mat();
		Mat sobelXMat=new Mat();
		Mat absMat=new Mat();
		Mat absXMat=new Mat();
		Mat dst=new Mat();
		//���������룬�����ͼ�����ȣ�x������y������ģ���С������3����������Ĭ��ֵ,�����ֻ��x����sobel������Ϊ�˷�ֹˮƽ����ı�Ե��Ӱ��
		Imgproc.Sobel(bluredMat, sobelMat, ddepth, 1, 0, 3, scale, delta,Core.BORDER_DEFAULT);
		//ˮƽ�����sobel
//		Imgproc.Sobel(bluredMat, sobelXMat, ddepth, 0, 1, 3, scale, delta,Core.BORDER_DEFAULT);
		//�����Ҫ���ڼ���sobelx����ĵ�����ʱ�򣬽�����ܲ���0--255��Χ�ڣ���Ҫ�Լ���֮��Ľ��ȡ����ֵ
		Core.convertScaleAbs(sobelMat, absMat);
//		Core.convertScaleAbs(sobelXMat, absXMat);
		//��ˮƽ�ʹ�ֱ������ݶȽ��мӺ�
//		Core.addWeighted(absMat, 0.5, absXMat, 0.5, 0, dst);
//		Imshow.imshow(absMat);
		//��ͼ����ж�ֵ��
		Mat binaryMat=new Mat();
		//���������룬�������ֵ��ʹ�ô����ֵ���������ֵ�����ͣ�����Ϊ0����1��
		Imgproc.threshold(absMat, binaryMat, Imgproc.THRESH_OTSU, 255,0);
//		Imgproc.adaptiveThreshold(absMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -10);
//		Imshow.imshow(binaryMat);
		//������
		Mat closedMat=new Mat();
		//������ģ��Ĵ�С
		Mat element=Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(morphSizeWidth,morphSizeHeight));
		Imgproc.morphologyEx(binaryMat, closedMat, Imgproc.MORPH_CLOSE, element);
//		Imshow.imshow(closedMat);
		//��������
		List<MatOfPoint> contours=new ArrayList<MatOfPoint>(); //�������
		Mat hierarchy=new Mat(); //�������֮��İ�����ϵ���ò���
		Imgproc.findContours(closedMat, contours, hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
		//����������ת��Ӿ��Σ���������������ֵ����ɸѡ
		for(int i=0; i<contours.size(); i++) {
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
//		//�ڶ�ֵͼ�����������������Ҫ��Ϊ�˿�һ��Ч��
//		for(int i=0; i<results.size(); i++) {
//			Imgproc.drawContours(original, contours, i,new Scalar(0,0,255),1);
//			System.out.println("��������");
//		}
//		Imshow.imshow(original);
		System.out.println("���������������"+results.size());
	}
}
