package com.xjtu.recognize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.ml.ANN_MLP;

import com.xjtu.ann.CharFeatures_1;
import com.xjtu.ann.ExtractANNFeature;
import com.xjtu.ann.PatternMatch;
import com.xjtu.ann.TrainANNModel;

public class CharRecognize {
	
	//Annģ�͵Ĵ�ŵص�
	private static String ANNModelPath="E:/Eclipse/LPR/charsANN.xml";
	
	/**
	 * ʹ��ann���ַ�����ʶ��
	 * @param chars List<Mat>
	 * 						�������еĲü��ַ�ͼ��
	 * @return String 
	 * 						����ʶ�������ַ���
	 */
	public static String charRecognize(List<Mat> chars) {
		
		//Ӣ�����ַ��ŷ����
		String[]    charLib= {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				           	  "A", "B", "C", "D", "E", "F", "G", "H", "J", "K",
				              "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V",
				              "W", "X", "Y", "Z"};
		//���ƺ��ֵķ����
        String []cCharacter= {"��", "��", "��", "��", "��", "��", "��", "��", "��", "��",
				              "��", "��", "��", "³", "��", "��", "��", "��", "��", "��",
				              "��", "��", "��", "��", "��", "ԥ", "��", "��", "��", "��", 
				              "��"};
        
        //String []cCharacter= {"��","��","��","��", "��","��","��","��","��","��","��","ԥ","��","��",
        //						"��","��","��","��","��","��","��","��","³","��","��","��","��",      
        //        				"��", "��","��","��"};
        
        //����ANN����
		ANN_MLP ann=null;
		File file=new File(ANNModelPath);
		//����annģ�ͣ��������ֱ�Ӽ���
		if(file.exists()) {
			ann=ANN_MLP.load(ANNModelPath);
		}else {
			//����ļ�������ֱ��ѵ��ģ�ͣ�Ӣ���ַ��������ַ��ֿ�ѵ��
			TrainANNModel.trainANNModel(0);
			TrainANNModel.trainANNModel(1);
		}
		//���Ԥ����ַ����
		float annRes = 0;
		String charSet="";
		//�����ַ�Ԥ��
		Mat chara=chars.get(0);
		int position=ChineseRecognize.chineseRecognize(chara, false);
//		int position=PatternMatch.patternMatch(chara);
		System.out.println(position);
		charSet=charSet+cCharacter[position];
		//Ӣ���ַ�Ԥ��		
		//��ʼ�������е����֡��ַ� ��ͼ��
		for(int i=1; i<chars.size(); i++) {
			//���һ���ַ�ͼ��
			Mat character=chars.get(i);
			//��ȡ���ַ���ann������60ά�ȵ�������
//			Mat feature=ExtractANNFeature.extractANNFeature(character);
//			Mat feature=ExtractANNFeature.extractANNFeature_1(character);
			Mat feature=CharFeatures_1.charFeatures_1(character);
//			Mat feature=CharFeatures_1.charFeatures_1(character);
			Mat results=new Mat();
			//Ԥ�����������results��
			annRes=ann.predict(feature, results, 1);
			System.out.println(annRes);
			double max=-2;
			int index=0;
			//
			System.out.println("Ӣ������������� "+results.size());
			//��ѯԤ����,��ֵ�����Ǹ�
			for(int col=0; col<results.cols(); col++) {
				double t=results.get(0, col)[0];
				//Ѱ������ֵ
				if(t>max) {
					max=t;
					index=col;
				}
			}
			//����õ�λ��Ϊ1����ô��ΪΪԤ����
			charSet=charSet+charLib[index];
			//
//			for(int row=0; row<results.rows(); row++) {
//				for(int col=0; col<results.cols(); col++) {
//					System.out.print(results.get(row, col)[0]+"\t");
//				}
//				System.out.println();
//			}
		}

		return charSet;
	}

}
