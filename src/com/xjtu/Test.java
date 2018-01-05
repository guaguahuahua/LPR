package com.xjtu;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.imgcodecs.Imgcodecs;

import com.xjtu.charseperate.SeperateChars;
import com.xjtu.classify.AreaSelect;
import com.xjtu.classify.SVMClassify;
import com.xjtu.preprocess.ColorLocate;
import com.xjtu.preprocess.SobelLocate;
import com.xjtu.recognize.CharRecognize;
import com.xjtu.util.DeskewAndAffine;
import com.xjtu.util.Imshow;
import com.xjtu.util.Resize1;

public class Test {
	
		private static String returnWords="���������գ�";
		
		/**
		 * ����Ƿ���˴������̵Ŀ�ʼ
		 * @param filePath �ӷ���˻�ȡ��ͼ���ڱ��صĴ�ž��Ե�ַ
		 * @return String ����˴���ͼ������ʶ��Ľ��
		 */
		public static String test(String filePath) {
			//����opencv�⣬������tomcat��������������Ҫʹ�þ���·������ʹ�������δע�͵����
			//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			System.load("C:\\opencv-3.3.0\\build\\java\\x64\\opencv_java330.dll");
			//��������ͼ���λ��
			String file = "E:/CuttedImg/1 (4).jpg";
			//�����·���Ǵӿͻ��˻�õ�ͼ���ڷ���˵Ĵ��λ��
//			String file=filePath;
			//����ͼ��
			Mat original=Imgcodecs.imread(file);
			Mat outputMat=new Mat();
			//������Ŷ�λ������Ϊ���Ƶ�����
			List<RotatedRect> results=new ArrayList<RotatedRect>();
			//ʹ��sobel��λ
			SobelLocate.sobelLocate(original,results);
			//��ԭͼ���������Ӿ�������
			//DrawBoundingRectangle.drawBoundingRectangle(original, results);
			//�����б������֮���ͼ��
			List<Mat> res=new ArrayList<Mat>();
			//��������Ϊ��ԭͼ������ͼ���и�ͼ
			DeskewAndAffine.deskewAndAffine(original, results, res);
			//ʹ����ɫ��λ
			List<RotatedRect> colorRes=new ArrayList<RotatedRect>();
			ColorLocate.colorLocate(original, 1, colorRes);
			//���Ӧ�õ�����б���������ͷָ����������������ֵ��������зָ��ת��
			System.out.println("colorLocate:"+colorRes.size());
			//��ԭͼ���������Ӿ�������
			//DrawBoundingRectangle.drawBoundingRectangle(original, colorRes);
			//Imshow.imshow(original);
			//�����б������֮���ͼ��
			List<Mat> res1=new ArrayList<Mat>();
			//��������Ϊ��ԭͼ������ͼ���и�ͼ
			DeskewAndAffine.deskewAndAffine(original, colorRes, res1);
			//ʹ��svm�Դ�����������ж�
			List <Mat> pengdingImg=new ArrayList <Mat>();
			//�����ַ�ʽ�ü���ͼ�����ͬһ��������
			pengdingImg.addAll(res1);
			pengdingImg.addAll(res);
			//����SVMģ�ͶԴ���������з���
			List<Mat> classifiedImg=new ArrayList<Mat>();
			SVMClassify.svmClassify(pengdingImg, classifiedImg);
//			//չʾ�������ͼ��
//			for(int i=0; i<classifiedImg.size(); i++) {
//				Imshow.imshow(classifiedImg.get(i));
//			}
//			System.out.println(classifiedImg.size());
			//�����ʹ����һ����ɫ�����ֵ����ɸѡ���ƺͷǳ�������
			Mat last=AreaSelect.areaSelect(classifiedImg);
			if(last!=null) {
//				Imshow.imshow(last);
			}else {
				System.out.println("��λʧ�ܻ�����svm�ж�ʧ�ܣ� ");
				return returnWords;
			}
			//�ַ����з�
			//����з�֮�󵥸��ַ���ͼ��
			List<Mat> charsImg=new ArrayList<Mat>(); 
			int check=SeperateChars.seperateChars(last, charsImg);
			if(check<0) {
				return returnWords;
			}
			//�鿴һ���зֳ����ַ��Ĵ�С
			System.out.println("�鿴һ���зֳ����ַ��Ĵ�С");
			//��Ź�һ������ַ�ͼ��
			List <Mat>resizedImgs=new ArrayList<Mat>();
			for(int i=0; i<charsImg.size(); i++) {
				Mat mat=Resize1.resize1(charsImg.get(i));
				resizedImgs.add(mat);
//				Imshow.imshow(mat);
			}
			//����ANN���з�֮����ַ�����ʶ�𣬲��������
			String carString=CharRecognize.charRecognize(resizedImgs);
			System.out.println(carString);
			return carString;
		}
		
		public static void main(String[] args) {
			test("");
		}
}
