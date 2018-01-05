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
	 * 使用svm对裁剪得到的车牌进行判断，最终只保留模型预测为车牌的图像，
	 * @param pengdingImg 裁剪得到的所有图像
	 * @param classifiedImg 最终判定为车牌的图像
	 */
	public static void svmClassify(List<Mat>pengdingImg, List<Mat> classifiedImg) {
		//初始化svm对象,直接加载训练好的svm模型
		SVM svm=null;
		File file=new File(filePath);
		//如果模型文件存在，那么直接加载，否则输出错误信息
		if(file.exists()) {
			svm=SVM.load(filePath);
		}else{
			System.out.println("SVMCLassify line 26"+"svm model doesn't exsit !");
		}
		//遍历所有的待定图像
		for(int count=0; count<pengdingImg.size(); count++) {
			//取出一张待定图像
			Mat img=pengdingImg.get(count);
			//抽取图像的特征,1维 （36+136）列
			Mat feature=ExtractFeature.extractFeature(img);
			//图像转化为CvSVM要求格式
			feature.convertTo(feature, CvType.CV_32FC1);
			//预测当前的图像是否为车牌
			int svmP=(int) svm.predict(feature);
			//如果判定为车牌图像， 那么将原图添加到结果集
			if(svmP==1) {
				classifiedImg.add(img);
			}
		}
	}
}
