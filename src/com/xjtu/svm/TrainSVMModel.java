package com.xjtu.svm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.ml.Ml;
import org.opencv.ml.SVM;

public class TrainSVMModel {
	private final static int amount=400; //加载图像各自的数量
	private final static int trainNums=300; //训练图像各自的数量
	private static int width; //训练图像的宽和高
	private static int height;
	private static String fileName="E:/Eclipse/LPR/svm.xml"; //存放svm模型的路径和文件的名称

	/**
	 * 读入有车牌的训练集
	 * @param mat Mat
	 * 				训练图像存放特征的Mat集合
	 * @param label List<Byte>
	 * 				每一副图像标签对应的标签存放在这个集合
	 * @param testImg Mat
	 * 				测试图像的特征集合
	 * @param labels List<Byte>
	 * 				测试图像标签存放集合
	 */
	public static void readInPlateImg(Mat mat, List<Byte>label, Mat testImg, List<Byte>labels) {
		String path="E:/HasPlate";
		File file=new File(path);
		//获得该路径下面的所有文件，并存放在文件数组中
		File[] fileArray=file.listFiles();
		if(fileArray.length==0) {
			System.out.println("文件夹为空，没有图像读入！");
		}
		//创建存放图像的list
		for(int i=0; i<amount; i++) {
			//获得文件的绝对路径
			path=fileArray[i].getAbsolutePath();
			//将该图像以mat 的格式读入
			Mat tmp=Imgcodecs.imread(path);
			//这是为后面合并两个集合做准备
			width=tmp.cols();
			height=tmp.rows();
			Mat feature=ExtractFeature.extractFeature(tmp);
			//将该图像添加到对应的数组中去
			if(i<trainNums) { //前300张图像加载到训练集
				mat.push_back(feature);
				label.add((byte)1);
			}else { //后100张图像加载测试集
				testImg.push_back(feature);
				labels.add((byte)1);
			}
		}
	}
	
	/**
	 * 读入无车牌的训练集
	 * @param mat Mat
	 * 				训练图像特征存放集合
	 * @param label List<Byte>
	 * 				训练图像的标签集合
	 * @param testImg Mat
	 * 				测试图像特征集合
	 * @param labels List<Byte>
	 * 				测试图像的标签集合
	 */
	public static void readInNoPlateImg(Mat mat, List<Byte>label, Mat testImg, List<Byte>labels) {
		String path="E:/NoPlate";
		File file=new File(path);
		File[] fileArray=file.listFiles();
		if(fileArray.length==0) {
			System.out.println("文件夹为空，没有图像读入！");
		}
		//创建存放图像的list
		for(int i=0; i<amount; i++) {
			//获得文件的绝对路径
			path=fileArray[i].getAbsolutePath();
			//将该图像以mat 的格式读入
			Mat tmp=Imgcodecs.imread(path);
			Mat feature=ExtractFeature.extractFeature(tmp);
			//将该图像添加到对应的数组中去
			if(i<trainNums) {
				mat.push_back(feature); //直接将一个 36*136 的图像添加到mat的最后面
				label.add((byte)0);
			}else {
				testImg.push_back(feature);
				labels.add((byte)0);
			}
		}
	}
	
	
	/**
	 * 训练SVM 并将训练好的模型写进xml文件
	 * @param trainningData Mat
	 * 						训练数据的特征集
	 * @param clusses Mat
	 * 						训练数据的标签集
	 * 注意 :
	 *    trainningData CV_32FC1
	 *    clusses       CV_32SC1
	 */
	public static void trainSVM(Mat trainningData, Mat clusses) {
		//数据变成了opencv需要的数据格式,下面进行svm的训练，
				SVM svm =SVM.create();
				svm.setType(SVM.C_SVC);
				svm.setKernel(SVM.RBF);
				svm.setDegree(0.1);
				svm.setGamma(1);
				svm.setCoef0(0);
				svm.setC(1);
				svm.setNu(0.1);
				svm.setP(0.1);
				TermCriteria t=new TermCriteria(TermCriteria.MAX_ITER, 20000, 0.0001);
				svm.setTermCriteria(t);
				System.out.println("trainningDataType: "+CvType.typeToString(trainningData.type()));
				System.out.println("classesType: "+CvType.typeToString(clusses.type()));
				System.out.println(trainningData.rows()==clusses.rows());
				
				svm.trainAuto(trainningData, 
						Ml.ROW_SAMPLE, //表明是样本是行存储还是列存储
						clusses,
						10,
						SVM.getDefaultGridPtr(SVM.C),
						SVM.getDefaultGridPtr(SVM.GAMMA),
						SVM.getDefaultGridPtr(SVM.P),
						SVM.getDefaultGridPtr(SVM.NU),
						SVM.getDefaultGridPtr(SVM.COEF),
						SVM.getDefaultGridPtr(SVM.DEGREE),
						true);
				//将svm模型写入到xml文件
				svm.save(fileName);	
				System.out.println("SVM 模型训练完毕！");
	}

	/**
	 * 测试主函数，其中包含了比较重要的一些部分：
	 * 数据转换部分： 
	 * 	训练数据集的数据类型 CvType.CV_32FC1
	 *  标签数据集的数据类型 CvType.CV_32SC1
	 * svm的加载部分：
	 * 	因为load(filePath)方法是个static方法，所以自己的svm如果要加载的话
	 *  需要从SVM.load(filePath)加载， 而不是从定义好的 svm.load(filePath)中加载，
	 *  第二种方式加载会返回一个新的对象，从而导致错误
	 * @param args
	 */
	public static void main(String []args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//加载有车牌和无车牌的图像，并并进行标记
		List<Byte> label=new ArrayList<Byte>();
		Mat trainningSet=new Mat();
		Mat trainningData=new Mat();
		//存放分类标签,标签的行数是训练图像的总数目，所以乘2
		Mat classes=new Mat(2*trainNums, 1, CvType.CV_8UC1); 
		Mat clusses=new Mat();
		//存放测试图像和标签的mat
		Mat testImg=new Mat(); 
		List<Byte> labels=new ArrayList<Byte>();
		//按照路径加载训练数据
		readInPlateImg(trainningSet, label, testImg, labels);
		readInNoPlateImg(trainningSet, label, testImg, labels);
		//将图片数据转换为CvSVM要求的数据格式
		trainningSet.convertTo(trainningData, CvType.CV_32FC1);
		//现在将label转换为mat类型，单通道，因为没有合适的接口，所以自己写
		for(int i=0; i<label.size(); i++) {
			//标签是单通道的Mat，并且只有一列，由0和1组成
			byte[] t=new byte[1];
			t[0]=label.get(i);
			classes.put(i, 0, t);
		}
		//将标签类型转化为CvSVM要求的数据格式
		classes.convertTo(clusses, CvType.CV_32SC1); //注意类型必须使用CV_32S，对应的是整数类型
		System.out.println("clusses.size: "+clusses.size());
		//加载svm.xml文件
		File file=new File(fileName);
		//初始化svm
		SVM svm=null;
		//判断系统有没有训练好的模型文件
		if(file.exists()) {
			//如果有训练好的模型文件，那么直接加载到svm
			//因为load是一个静态方法，所以如果要给当前的svm加载训练好的模型的话还是要通过类方法
			svm=SVM.load(fileName);
		//如果没有训练好的模型文件，那么调用系统的svm直接训练模型，并将训练好的模型保存在svm.xml中去
		}else {
			System.out.println("SVM 模型不存在，开始训练....");
			trainSVM(trainningData, clusses);
		}
		System.out.println("标签集地 数量："+labels.size());
		System.out.println("训练数据量: "+trainningData.rows()+", 训练数据特征数: "+trainningData.cols());
		//统计预测结果和原类型相同的个数
		int counting=0;
		//将测试数据的Mat进行加载， svm进行一个预测
		for(int row=0; row<testImg.rows(); row++) {
			//从测试数据集中得到一行（测试图像是抽取136+36维度特征之后按照行存储的，所以每一行特征向量表示一副图像）
			//剪切函数，从mat中剪切出一行，testImg中一行就是一幅图像，所以裁剪出这一行
			Mat p=new Mat(testImg, new Range(row, row+1), new Range(0,testImg.cols())); 
			//将这行数据转化为CvSVM中需要的格式
			p.convertTo(p, CvType.CV_32F);
			//输入到模型中进行判断，输出结果是一个浮点类型， 我们将它转化为整型
			int svmP=(int)svm.predict(p); //svm模型判断当前图像的类型
			//获取判定图像的标签
			int cla=labels.get(row);
			//如果判定结果和该图像的标签一致，我们认为预测成功，给计数器+1
			if(svmP==cla) {
				counting++;
			}
		}
		//输出最后的预测准确率
		double rate=(double) counting/(double) labels.size();
//		System.out.println("C: "+svm.getC());
//		System.out.println("coef: "+svm.getCoef0());
//		System.out.println("degree: "+svm.getDegree());
//		System.out.println("gamma: "+svm.getGamma());
//		System.out.println("kernel: "+svm.getKernelType());
//		System.out.println("nu: "+svm.getNu());
//		System.out.println("p: "+svm.getP());
//		System.out.println("type: "+svm.getType());
		System.out.println("判断准确图像："+counting+", "+"总共图像数量："+labels.size()+", 判定准确率："+rate);
	}
}
