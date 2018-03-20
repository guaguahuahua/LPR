package com.xjtu.ann;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.ANN_MLP;
import org.opencv.ml.Ml;

public class TrainANNModel {
	
	public static String filePath="E:/ann"; //字符训练的路径
	public static String testImgPath="E:/annTest";
	public static int charsNum=34;  //总的类的个数
	public static int chineseNum=31;
	public static String ANNModelPath="E:/Eclipse/LPR/";
	public static int CHINESE=0;
	public static int CHARS=1;
	
	/**
	 * 训练ann模型，两种模型英文字符和中文字符分开训练
	 * @param type 表示训练的字符类型
	 * @return
	 */
	public static ANN_MLP trainANNModel(int type) {
		//存放训练数据
		Mat trainData=new Mat();
		if(type==CHINESE) {
			filePath+="/chinese";
		}else if(type==CHARS){
			filePath+="/chars";
		}else {
			System.out.println("训练类型输入错误！");
		}
		File file=new File(filePath);
		File []fileArray=file.listFiles();
		List<Integer>labels=new ArrayList<Integer>();
		//遍历文件数组，抽取每个文件的特征存放在ANN输入的Mat中
		File []fileList = null;
		for(int i=0; i<fileArray.length; i++) {
			if(fileArray[i].isDirectory()) {
				//获得该文件夹的绝对路径
				String path=fileArray[i].getAbsolutePath();
				//得到该文件夹下面的所有文件
				fileList=(new File(path)).listFiles();
			}
			labels.add(fileList.length);
			//读入该文件夹下面的所有文件
			for(int j=0; j<fileList.length; j++) {
				String singleFile=fileList[j].getAbsolutePath();
				Mat mat=Imgcodecs.imread(singleFile);
				//使用PCA抽取特征，6个主成分，120维向量作为字符特征
//				Mat features=ExtractANNFeature.extractANNFeature(mat);
//				Mat features=ExtractANNFeature.extractANNFeature_1(mat);
//				Mat dst = new Mat();
//				Imgproc.cvtColor(mat, dst, Imgproc.COLOR_RGB2GRAY);
//				Mat features=CharFeatures_1.charFeatures_1(mat);
//				Mat features=ChineseFeatures.chineseFeatures(mat);
//				Mat features=ChineseFeatures.LBPAndProjectFeatures(mat);
				Mat features=ChineseFeatures.gaborFeatures(mat);
				trainData.push_back(features);
			}
		}
		
		System.out.println("trainData: "+trainData.rows()+", "+trainData.cols());
		//上面的数据集导入完成， 后面要做的就是ANN的训练
		ANN_MLP ann=ANN_MLP.create();
		//设置神经网络的参数
		//设置感知层的数量及每层的节点数量
		Mat layer=new Mat(1, 3, CvType.CV_32SC1);
		//输入层应该和训练数据的列数相等
		layer.put(0, 0, new int[] {trainData.cols()});
		//隐藏层的神经元的个数
		layer.put(0, 1, new int[] { 64 }); //字符训练的数量是40，中文字符数量是64
		//输出层个数应该和最后字符种类一样多
		layer.put(0, 2, new int[] {labels.size()});
		ann.setLayerSizes(layer);
		
		//设置激活函数类型
		ann.setActivationFunction(ANN_MLP.SIGMOID_SYM , 1, 1);
		//设置神经网络训练参数
		TermCriteria val=new TermCriteria(TermCriteria.MAX_ITER, 30000, 0.0001);
		ann.setTermCriteria(val);
		//设置训练算法
		ann.setTrainMethod(ANN_MLP.BACKPROP);
		ann.setBackpropWeightScale(0.1);
		ann.setBackpropMomentumScale(0.1);

		//设置标签的列数数目
		int classes=0;
		if(type==CHINESE) {
			classes=chineseNum;
		}else if(type==CHARS) {
			classes=charsNum;
		}
		//设置标签
		Mat response=new Mat(trainData.rows(), classes, CvType.CV_32FC1);
		System.out.println("label.size: "+labels.size());
		//计算一下每个字符文件夹中字符的数量，为了给不同位置打上标签做准备
		int t=0;
		List<Integer>nums=new ArrayList<Integer>();
		for(int index=0; index<labels.size(); index++) {
			t+=labels.get(index);
			nums.add(t);
		}
		System.out.println();
		//打上标签
		int j=0;
		int end;
		for(int i=0; i<labels.size(); i++) {
			int num=labels.get(i);
			end=j+num;
			//给相同数据行打上相同的标签
			for(; j<end; j++) {
				response.put(j, i, new float[] {1.f});
			}
		}
		//训练ann模型
		long start=System.currentTimeMillis();
		System.out.println("begin to train model");
		ann.train(trainData, Ml.ROW_SAMPLE, response);
		System.out.println("stop to train model");
		long end1=System.currentTimeMillis();
		System.out.println("cost time:"+(end1-start)/60000+" min");
		//将模型保存到项目路径下面
		if(type==CHINESE) {
			ANNModelPath+="/chineseANN.xml";
		}else if(type==CHARS) {
			ANNModelPath+="/charsANN.xml";
		}
		ann.save(ANNModelPath);
//		//测试输出标签，判断是否正常
//		for(int i=nums.get(63); i<nums.get(64); i++) {
//			for(int k=0; k<response.cols(); k++) {
//				System.out.print(response.get(i, k)[0]+"\t");
//			}
//			System.out.println();
//		}
		return ann;
	}
	
	/**
	 * 读入测试图像，打好标签
	 * @type 
	 * @return list 返回抽取特征之后的图像，以及它们的标签
	 */
	public static List readInTestImg(int type) {
		if(type==CHINESE) {
			testImgPath+="/chinese";
		}else {
			testImgPath+="/chars";
		}
		File testImg=new File(testImgPath);
		File []fileArray=testImg.listFiles();
		//存放数据标签
		ArrayList<Integer> labels=new ArrayList<Integer>();
		//存放测试数据
		Mat testData=new Mat();
		File []fileList=null;
		//读入测试文件，并抽取特征，存放在testData中
		for(int i=0; i<fileArray.length; i++) {
			if(fileArray[i].isDirectory()) {
				String path=fileArray[i].getAbsolutePath();
				//得到该文件夹下面的所有文件
				fileList=(new File(path)).listFiles();
			}
			labels.add(fileList.length);
			//读入该文件夹下面的所有文件
			for(int j=0; j<fileList.length; j++) {
				String singleFile=fileList[j].getAbsolutePath();
				//读入文件夹中的某个图
				Mat mat=Imgcodecs.imread(singleFile);				
//				Mat features=ExtractANNFeature.extractANNFeature(mat);
//				Mat features=ExtractANNFeature.extractANNFeature_1(mat);
//				Mat features=CharFeatures_1.charFeatures_1(mat);
//				Mat features=ChineseFeatures.chineseFeatures(mat);
//				Mat features=ChineseFeatures.LBPAndProjectFeatures(mat);
				Mat features=ChineseFeatures.gaborFeatures(mat);
				
//				Mat dst=new Mat();
//				Imgproc.cvtColor(mat, dst, Imgproc.COLOR_RGB2GRAY);
//				Mat features=ChineseFeatures.chineseFeatures(dst);
				testData.push_back(features);
			}
		}
		
		int classes=0;
		//根据刚才的读入的测试数据给它们生成标签mat
		if(type==CHINESE) {
			classes=chineseNum;
		}else if(type==CHARS){
			classes=charsNum;
		}
		Mat cluss=new Mat(testData.rows(), classes, CvType.CV_32FC1);
		int j=0;
		int end;
		for(int i=0; i<labels.size(); i++) {
			int num=labels.get(i);
			end=j+num;
			//给相同数据行打上相同的标签
			for(; j<end; j++) {
				cluss.put(j, i, new float[] {1.f});
			}
		}
		//计算一下每种字符文件夹中图像的数量
		int t=0;
		List<Integer>nums=new ArrayList<Integer>();
		for(int index=0; index<labels.size(); index++) {
			t+=labels.get(index);
			nums.add(t);
		}
//		System.out.println("testData.rows: "+cluss.rows());
//		for(int i=0; i<nums.get(1); i++) {
//			for(int k=0; k<cluss.cols(); k++) {
//				System.out.print(cluss.get(i, k)[0]+"\t");
//			}
//			System.out.println();
//		}
		List result=new ArrayList();
		//分别将抽取特征后的测试数据、标签数据、不同测试数据的个数放在数组中返回
		result.add(testData);
		result.add(cluss);
		result.add(nums);
		return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//		trainANNModel(CHINESE);
		//		输入测试数据的类型
		int type=CHINESE;
		//返回3个值 0：测试数据  1：数据标签 2：前n个文件夹中字符数量的和
		List receive=readInTestImg(type);
		Mat testData=(Mat) receive.get(0);
		Mat labels=(Mat) receive.get(1);
		List nums=(List) receive.get(2);
		ANN_MLP ann=null;
		if(type==CHINESE) {
			ANNModelPath+="/chineseANN.xml";
		}else if(type==CHARS) {
			ANNModelPath+="/charsANN.xml";
		}
		ann=ANN_MLP.load(ANNModelPath);
		//存放ann预测结果
		Mat res=new Mat();
		//测试
		for(int i=0; i<testData.rows(); i++) {
			//裁剪出测试集的一行
			Range rowRange=new Range(i, i+1);
			Range colRange=new Range(0, testData.cols());
			Mat temp=new Mat(testData, rowRange, colRange);
			//存放对这张图像的预测结果
			Mat result=new Mat();
			//使用ann模型进行预测的时候，输出的是一个图像的特征（Mat类型的），以及最终的预测结果，浮点数的形式，对于模型中的每个类别都给出了一个最终的预测结果，但是取最大的预测值作为我们最终的预测结果
			ann.predict(temp, result, 0);
			//将预测结果添加到总集合
			res.push_back(result);
		}
		//测试ann的准确率
		int count=0;
		int index=0;
		//在ann的判定中，最终的判定的结果是
		for(int cla=0; cla<nums.size(); cla++) {
			int t=(int) nums.get(cla);

			//在同一类数据集中遍历
			for(; index<t; index++) {
				//搜索这行中预测概率最大的位置
				int position=0;
				double max=-2;
				for(int k=0; k<res.cols(); k++) {
					double tmp=res.get(index, k)[0];
					if(tmp>max) {
						max=tmp;
						position=k;
					}
				}
				//判断预测值最大的位置是否和标签位置相同
				if(position==cla) {
					count++;
				}
			}

		}
		System.out.println("判定正确count:"+count);
		System.out.println("总数目testData: "+testData.rows());
		System.out.println("准确率："+count*1.0/testData.rows());
//		//输出预测结果集
//		for(int i1=0; i1<res.rows(); i1++) {
//			for(int j=0; j<res.cols(); j++) {
//				System.out.print(res.get(i1, j)[0]+"\t");
//			}
//			System.out.println();
//		}
	}
}
