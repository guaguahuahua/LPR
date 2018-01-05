package com.xjtu.recognize;

import org.opencv.core.Mat;
import org.opencv.ml.ANN_MLP;

import com.xjtu.ann.CharFeatures_1;
import com.xjtu.ann.ChineseFeatures;
import com.xjtu.ann.ExtractANNFeature;

public class ChineseRecognize {
	private static String ANNPath="E:/Eclipse/LPR/chineseANN.xml";
	/**
	 * �����ַ���ʶ��
	 * @param ann annģ��
	 * @param character �ַ�ͼ��
	 * @return ���ظ��ַ���Ӧ������ֵ
	 */
	public static int chineseRecognize(Mat character) {
		ANN_MLP ann=ANN_MLP.load(ANNPath);
		//��ȡ���ַ���ann������60ά�ȵ�������
//		Mat feature=ExtractANNFeature.extractANNFeature(character);
//		Mat feature=ExtractANNFeature.extractANNFeature_1(character);
//		Mat feature=CharFeatures_1.charFeatures_1(character);
//		Mat feature=ChineseFeatures.chineseFeatures(character);
//		Mat feature=ChineseFeatures.chineseFeatures(character);
		Mat feature=ChineseFeatures.gaborFeatures(character);
		Mat results=new Mat();
		//Ԥ�����������results��
		ann.predict(feature, results, 0);
		for(int col=0; col<results.cols(); col++) {
			System.out.print(results.get(0, col)[0]+"\t");
		}
		System.out.println();
		//
//		System.out.println("������������� "+results.size());
		//��¼������������ַ���λ��
		int index=0;
		double max=-2;
		//�鿴�����ַ���Ԥ����
		for(int col=0; col<results.cols(); col++) {
			double t=results.get(0, col)[0];
			//Ѱ������ֵ
			if(t>max) {
				max=t;
				index=col;
			}
		}
		//���������ַ���λ��
		System.out.println("Ԥ�⺺�ֵ�λ�ã�"+index);
		return index;
	}

}
