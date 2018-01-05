package com.xjtu.classify;

import java.io.File;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.SVM;

import com.xjtu.svm.ExtractFeature;

public class SVMClassify {

	private static String filePath="E:/Eclipse/LPR/svm.xml";
	/**
	 * ʹ��svm�Բü��õ��ĳ��ƽ����жϣ�����ֻ����ģ��Ԥ��Ϊ���Ƶ�ͼ��
	 * @param pengdingImg �ü��õ�������ͼ��
	 * @param classifiedImg �����ж�Ϊ���Ƶ�ͼ��
	 */
	public static void svmClassify(List<Mat>pengdingImg, List<Mat> classifiedImg) {
		//��ʼ��svm����,ֱ�Ӽ���ѵ���õ�svmģ��
		SVM svm=null;
		File file=new File(filePath);
		//���ģ���ļ����ڣ���ôֱ�Ӽ��أ��������������Ϣ
		if(file.exists()) {
			svm=SVM.load(filePath);
		}else{
			System.out.println("SVMCLassify line 26"+"svm model doesn't exsit !");
		}
		//�������еĴ���ͼ��
		for(int count=0; count<pengdingImg.size(); count++) {
			//ȡ��һ�Ŵ���ͼ��
			Mat img=pengdingImg.get(count);
			//��ȡͼ�������,1ά ��36+136����
			Mat feature=ExtractFeature.extractFeature(img);
			//ͼ��ת��ΪCvSVMҪ���ʽ
			feature.convertTo(feature, CvType.CV_32FC1);
			//Ԥ�⵱ǰ��ͼ���Ƿ�Ϊ����
			int svmP=(int) svm.predict(feature);
			//����ж�Ϊ����ͼ�� ��ô��ԭͼ��ӵ������
			if(svmP==1) {
				classifiedImg.add(img);
			}
		}
	}
}
