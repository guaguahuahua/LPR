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
	
	//Ann模型的存放地点
	private static String ANNModelPath="E:/Eclipse/LPR/charsANN.xml";
	
	/**
	 * 使用ann对字符进行识别
	 * @param chars List<Mat>
	 * 						输入所有的裁剪字符图像
	 * @return String 
	 * 						返回识别结果，字符串
	 */
	public static String charRecognize(List<Mat> chars) {
		
		//英文数字符号分类表
		String[]    charLib= {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				           	  "A", "B", "C", "D", "E", "F", "G", "H", "J", "K",
				              "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V",
				              "W", "X", "Y", "Z"};
		//车牌汉字的分类表
        String []cCharacter= {"川", "鄂", "赣", "甘", "贵", "桂", "黑", "沪", "冀", "津",
				              "京", "吉", "辽", "鲁", "蒙", "闽", "宁", "青", "琼", "陕",
				              "苏", "晋", "皖", "湘", "新", "豫", "渝", "粤", "云", "藏", 
				              "浙"};
        
        //String []cCharacter= {"皖","京","渝","闽", "甘","粤","桂","贵","琼","冀","黑","豫","鄂","湘",
        //						"苏","赣","吉","辽","蒙","宁","青","陕","鲁","沪","晋","川","津",      
        //        				"新", "藏","云","浙"};
        
        //创建ANN对象
		ANN_MLP ann=null;
		File file=new File(ANNModelPath);
		//加载ann模型，如果存在直接加载
		if(file.exists()) {
			ann=ANN_MLP.load(ANNModelPath);
		}else {
			//如果文件不存在直接训练模型，英文字符和中文字符分开训练
			TrainANNModel.trainANNModel(0);
			TrainANNModel.trainANNModel(1);
		}
		//存放预测的字符结果
		float annRes = 0;
		String charSet="";
		//中文字符预测
		Mat chara=chars.get(0);
		int position=ChineseRecognize.chineseRecognize(chara, false);
//		int position=PatternMatch.patternMatch(chara);
		System.out.println(position);
		charSet=charSet+cCharacter[position];
		//英文字符预测		
		//开始遍历所有的数字、字符 的图像，
		for(int i=1; i<chars.size(); i++) {
			//获得一张字符图像
			Mat character=chars.get(i);
			//抽取该字符的ann特征（60维度的向量）
//			Mat feature=ExtractANNFeature.extractANNFeature(character);
//			Mat feature=ExtractANNFeature.extractANNFeature_1(character);
			Mat feature=CharFeatures_1.charFeatures_1(character);
//			Mat feature=CharFeatures_1.charFeatures_1(character);
			Mat results=new Mat();
			//预测结果，存放在results中
			annRes=ann.predict(feature, results, 1);
			System.out.println(annRes);
			double max=-2;
			int index=0;
			//
			System.out.println("英数符类别数量： "+results.size());
			//查询预测结果,找值最大的那个
			for(int col=0; col<results.cols(); col++) {
				double t=results.get(0, col)[0];
				//寻找最大的值
				if(t>max) {
					max=t;
					index=col;
				}
			}
			//如果该点位置为1，那么认为为预测结果
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
