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

public class SobelLocate{
	
	/**
	 * ������ͼ����лһ�����ֵ��,��˹ģ������̬ѧ�����㴦��֮���ٽ����������ҹ���
	 * @param original Mat
	 * 					����ͼ��
	 * @param results List<RotatedRect>
	 * 					��ſ���Ϊ����ͼ��ļ���
	 * @param debug boolean 
	 * 					�Ƿ�������ģʽ
	 */
	public static void sobelLocate(Mat original, List<RotatedRect> results, boolean debug) {
		
		if(debug) {
			Imshow.imshow(original, "ԭʼ�ĳ���ͼ��.SobelLocate");
		}
		Mat grayMat=new Mat();
		//��ͼ����лҶȻ�
		Imgproc.cvtColor(original, grayMat, Imgproc.COLOR_RGB2GRAY);
		//ʹ��opencv�ҶȻ��������лһ�֮��3ͨ����1ͨ��������matԪ�ش�СҲ�����1
		if(debug) {
			Imshow.imshow(grayMat, "�ҶȻ���ͼ��.SobelLocate");
		}
		//�ԻҶ�ͼ���и�˹ģ������
		Size size=new Size(SobelLocateConstant.getGaussianblursize(), SobelLocateConstant.getGaussianblursize());
		Mat bluredMat =new Mat();
		Imgproc.GaussianBlur(grayMat, bluredMat, size, 0, 0);
		if(debug) {
			Imshow.imshow(bluredMat, "��˹ģ��֮���ͼ��.SobelLocate");
		}
		//sobel��Ե���
		Mat sobelMat=new Mat();
		Mat sobelXMat=new Mat();
		Mat absMat=new Mat();
		Mat absXMat=new Mat();
		Mat dst=new Mat();
		//���������룬�����ͼ�����ȣ�x������y������ģ���С������3����������Ĭ��ֵ,�����ֻ��x����sobel������Ϊ�˷�ֹˮƽ����ı�Ե��Ӱ��
		Imgproc.Sobel(bluredMat, sobelMat, SobelLocateConstant.getDdepth(), 1, 0, 3, SobelLocateConstant.getScale(), SobelLocateConstant.getDelta(), Core.BORDER_DEFAULT);
		//ˮƽ�����sobel
		//Imgproc.Sobel(bluredMat, sobelXMat, ddepth, 0, 1, 3, scale, delta,Core.BORDER_DEFAULT);
		//�����Ҫ���ڼ���sobelx����ĵ�����ʱ�򣬽�����ܲ���0--255��Χ�ڣ���Ҫ�Լ���֮��Ľ��ȡ����ֵ
		Core.convertScaleAbs(sobelMat, absMat);
		//Core.convertScaleAbs(sobelXMat, absXMat);
		//��ˮƽ�ʹ�ֱ������ݶȽ��мӺ�
		//Core.addWeighted(absMat, 0.5, absXMat, 0.5, 0, dst);
		if(debug) {
			Imshow.imshow(absMat, "sobel��Ե���֮���ͼ��.SobelLocate");
		}
		//��ͼ����ж�ֵ��
		Mat binaryMat=new Mat();
		//���������룬�������ֵ��ʹ�ô����ֵ���������ֵ�����ͣ�����Ϊ0����1��
		Imgproc.threshold(absMat, binaryMat, Imgproc.THRESH_OTSU, 255,0);
		//Imgproc.adaptiveThreshold(absMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -10);
		if(debug) {
			Imshow.imshow(binaryMat, "��ֵ�����ͼ��.SobelLocate");
		}
		//������
		Mat closedMat=new Mat();
		//������ģ��Ĵ�С
		Mat element=Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(SobelLocateConstant.getMorphsizewidth(), SobelLocateConstant.getMorphsizeheight()));
		Imgproc.morphologyEx(binaryMat, closedMat, Imgproc.MORPH_CLOSE, element);
		if(debug) {
			Imshow.imshow(closedMat, "��̬ѧ������֮���ͼ��.SobelLocate");
		}
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
		//�ڶ�ֵͼ�����������������Ҫ��Ϊ�˿�һ��Ч��
		if(debug) {
			for(int i=0; i<results.size(); i++) {
				Imgproc.drawContours(original, contours, i, new Scalar(0,0,255),1);
			}
			Imshow.imshow(original);
			System.out.println("���������������"+results.size());
		}
	}
	
	
	/**
	 * �����ȡ�׶��Ե�ͼ����д����
	 * @param args
	 */
	public static void main(String []args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String path="E:\\CuttedImg\\1 (1).jpg";
		Mat rgbImg=Imgcodecs.imread(path);
		Imshow.imshow(rgbImg, "����ͼ�����");
	}
	
}
