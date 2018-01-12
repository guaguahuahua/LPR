package com.xjtu.recognize;

import org.opencv.core.Mat;
import org.opencv.ml.ANN_MLP;

import com.xjtu.ann.CharFeatures_1;
import com.xjtu.ann.ChineseFeatures;
import com.xjtu.ann.ExtractANNFeature;

public class ChineseRecognize {

	//中文字符Ann识别模型的存放路径
	private static String ANNPath="E:/Eclipse/LPR/chineseANN.xml";
	
	/**
	 * 中文字符的识别
	 * @param character Mat
	 * 					待识别的中文字符的图像
	 * @param debug boolean 
	 * 					是否显示处理细节
	 * @return int
	 * 					对中文字符的分类索引号
	 */
	public static int chineseRecognize(Mat character, boolean debug) {
		ANN_MLP ann=ANN_MLP.load(ANNPath);
		//抽取该字符的ann特征,
		//Mat feature=ExtractANNFeature.extractANNFeature(character);
		//Mat feature=ExtractANNFeature.extractANNFeature_1(character);
		//Mat feature=CharFeatures_1.charFeatures_1(character);
		//Mat feature=ChineseFeatures.chineseFeatures(character);
		//Mat feature=ChineseFeatures.chineseFeatures(character);
		//抽取中文字符的特征，使用gabor特征，4个方向共计1600维度
		Mat feature=ChineseFeatures.gaborFeatures(character);
		Mat results=new Mat();
		//预测结果，存放在results中
		ann.predict(feature, results, 0);
		if(debug) {
			System.out.println("ChineseRecognize.java, 输出一个中文字符预测结果，即对每个类的概率计算值");
			for(int col=0; col<results.cols(); col++) {
				System.out.print(results.get(0, col)[0]+"\t");
			}
			System.out.println();	
		}
		//
		//System.out.println("汉字类别数量： "+results.size());
		//记录概率最大中文字符的位置
		int index=0;
		double max=-2;
		//查看中文字符的预测结果
		for(int col=0; col<results.cols(); col++) {
			double t=results.get(0, col)[0];
			//寻找最大的值
			if(t>max) {
				max=t;
				index=col;
			}
		}
		//返回中文字符的位置
		System.out.println("预测汉字的位置："+index);
		return index;
	}

}
