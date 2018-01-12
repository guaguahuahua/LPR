package com.xjtu.recognize;

import org.opencv.core.Mat;
import org.opencv.ml.ANN_MLP;

import com.xjtu.ann.CharFeatures_1;
import com.xjtu.ann.ChineseFeatures;
import com.xjtu.ann.ExtractANNFeature;

public class ChineseRecognize {

	//�����ַ�Annʶ��ģ�͵Ĵ��·��
	private static String ANNPath="E:/Eclipse/LPR/chineseANN.xml";
	
	/**
	 * �����ַ���ʶ��
	 * @param character Mat
	 * 					��ʶ��������ַ���ͼ��
	 * @param debug boolean 
	 * 					�Ƿ���ʾ����ϸ��
	 * @return int
	 * 					�������ַ��ķ���������
	 */
	public static int chineseRecognize(Mat character, boolean debug) {
		ANN_MLP ann=ANN_MLP.load(ANNPath);
		//��ȡ���ַ���ann����,
		//Mat feature=ExtractANNFeature.extractANNFeature(character);
		//Mat feature=ExtractANNFeature.extractANNFeature_1(character);
		//Mat feature=CharFeatures_1.charFeatures_1(character);
		//Mat feature=ChineseFeatures.chineseFeatures(character);
		//Mat feature=ChineseFeatures.chineseFeatures(character);
		//��ȡ�����ַ���������ʹ��gabor������4�����򹲼�1600ά��
		Mat feature=ChineseFeatures.gaborFeatures(character);
		Mat results=new Mat();
		//Ԥ�����������results��
		ann.predict(feature, results, 0);
		if(debug) {
			System.out.println("ChineseRecognize.java, ���һ�������ַ�Ԥ����������ÿ����ĸ��ʼ���ֵ");
			for(int col=0; col<results.cols(); col++) {
				System.out.print(results.get(0, col)[0]+"\t");
			}
			System.out.println();	
		}
		//
		//System.out.println("������������� "+results.size());
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
