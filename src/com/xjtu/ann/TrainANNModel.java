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
	
	public static String filePath="E:/ann"; //�ַ�ѵ����·��
	public static String testImgPath="E:/annTest";
	public static int charsNum=34;  //�ܵ���ĸ���
	public static int chineseNum=31;
	public static String ANNModelPath="E:/Eclipse/LPR/";
	public static int CHINESE=0;
	public static int CHARS=1;
	
	/**
	 * ѵ��annģ�ͣ�����ģ��Ӣ���ַ��������ַ��ֿ�ѵ��
	 * @param type ��ʾѵ�����ַ�����
	 * @return
	 */
	public static ANN_MLP trainANNModel(int type) {
		//���ѵ������
		Mat trainData=new Mat();
		if(type==CHINESE) {
			filePath+="/chinese";
		}else if(type==CHARS){
			filePath+="/chars";
		}else {
			System.out.println("ѵ�������������");
		}
		File file=new File(filePath);
		File []fileArray=file.listFiles();
		List<Integer>labels=new ArrayList<Integer>();
		//�����ļ����飬��ȡÿ���ļ������������ANN�����Mat��
		File []fileList = null;
		for(int i=0; i<fileArray.length; i++) {
			if(fileArray[i].isDirectory()) {
				//��ø��ļ��еľ���·��
				String path=fileArray[i].getAbsolutePath();
				//�õ����ļ�������������ļ�
				fileList=(new File(path)).listFiles();
			}
			labels.add(fileList.length);
			//������ļ�������������ļ�
			for(int j=0; j<fileList.length; j++) {
				String singleFile=fileList[j].getAbsolutePath();
				Mat mat=Imgcodecs.imread(singleFile);
				//ʹ��PCA��ȡ������6�����ɷ֣�120ά������Ϊ�ַ�����
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
		//��������ݼ�������ɣ� ����Ҫ���ľ���ANN��ѵ��
		ANN_MLP ann=ANN_MLP.create();
		//����������Ĳ���
		//���ø�֪���������ÿ��Ľڵ�����
		Mat layer=new Mat(1, 3, CvType.CV_32SC1);
		//�����Ӧ�ú�ѵ�����ݵ��������
		layer.put(0, 0, new int[] {trainData.cols()});
		//���ز����Ԫ�ĸ���
		layer.put(0, 1, new int[] { 64 }); //�ַ�ѵ����������40�������ַ�������64
		//��������Ӧ�ú�����ַ�����һ����
		layer.put(0, 2, new int[] {labels.size()});
		ann.setLayerSizes(layer);
		
		//���ü��������
		ann.setActivationFunction(ANN_MLP.SIGMOID_SYM , 1, 1);
		//����������ѵ������
		TermCriteria val=new TermCriteria(TermCriteria.MAX_ITER, 30000, 0.0001);
		ann.setTermCriteria(val);
		//����ѵ���㷨
		ann.setTrainMethod(ANN_MLP.BACKPROP);
		ann.setBackpropWeightScale(0.1);
		ann.setBackpropMomentumScale(0.1);

		//���ñ�ǩ��������Ŀ
		int classes=0;
		if(type==CHINESE) {
			classes=chineseNum;
		}else if(type==CHARS) {
			classes=charsNum;
		}
		//���ñ�ǩ
		Mat response=new Mat(trainData.rows(), classes, CvType.CV_32FC1);
		System.out.println("label.size: "+labels.size());
		//����һ��ÿ���ַ��ļ������ַ���������Ϊ�˸���ͬλ�ô��ϱ�ǩ��׼��
		int t=0;
		List<Integer>nums=new ArrayList<Integer>();
		for(int index=0; index<labels.size(); index++) {
			t+=labels.get(index);
			nums.add(t);
		}
		System.out.println();
		//���ϱ�ǩ
		int j=0;
		int end;
		for(int i=0; i<labels.size(); i++) {
			int num=labels.get(i);
			end=j+num;
			//����ͬ�����д�����ͬ�ı�ǩ
			for(; j<end; j++) {
				response.put(j, i, new float[] {1.f});
			}
		}
		//ѵ��annģ��
		long start=System.currentTimeMillis();
		System.out.println("begin to train model");
		ann.train(trainData, Ml.ROW_SAMPLE, response);
		System.out.println("stop to train model");
		long end1=System.currentTimeMillis();
		System.out.println("cost time:"+(end1-start)/60000+" min");
		//��ģ�ͱ��浽��Ŀ·������
		if(type==CHINESE) {
			ANNModelPath+="/chineseANN.xml";
		}else if(type==CHARS) {
			ANNModelPath+="/charsANN.xml";
		}
		ann.save(ANNModelPath);
//		//���������ǩ���ж��Ƿ�����
//		for(int i=nums.get(63); i<nums.get(64); i++) {
//			for(int k=0; k<response.cols(); k++) {
//				System.out.print(response.get(i, k)[0]+"\t");
//			}
//			System.out.println();
//		}
		return ann;
	}
	
	/**
	 * �������ͼ�񣬴�ñ�ǩ
	 * @type 
	 * @return list ���س�ȡ����֮���ͼ���Լ����ǵı�ǩ
	 */
	public static List readInTestImg(int type) {
		if(type==CHINESE) {
			testImgPath+="/chinese";
		}else {
			testImgPath+="/chars";
		}
		File testImg=new File(testImgPath);
		File []fileArray=testImg.listFiles();
		//������ݱ�ǩ
		ArrayList<Integer> labels=new ArrayList<Integer>();
		//��Ų�������
		Mat testData=new Mat();
		File []fileList=null;
		//��������ļ�������ȡ�����������testData��
		for(int i=0; i<fileArray.length; i++) {
			if(fileArray[i].isDirectory()) {
				String path=fileArray[i].getAbsolutePath();
				//�õ����ļ�������������ļ�
				fileList=(new File(path)).listFiles();
			}
			labels.add(fileList.length);
			//������ļ�������������ļ�
			for(int j=0; j<fileList.length; j++) {
				String singleFile=fileList[j].getAbsolutePath();
				//�����ļ����е�ĳ��ͼ
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
		//���ݸղŵĶ���Ĳ������ݸ��������ɱ�ǩmat
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
			//����ͬ�����д�����ͬ�ı�ǩ
			for(; j<end; j++) {
				cluss.put(j, i, new float[] {1.f});
			}
		}
		//����һ��ÿ���ַ��ļ�����ͼ�������
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
		//�ֱ𽫳�ȡ������Ĳ������ݡ���ǩ���ݡ���ͬ�������ݵĸ������������з���
		result.add(testData);
		result.add(cluss);
		result.add(nums);
		return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//		trainANNModel(CHINESE);
		//		����������ݵ�����
		int type=CHINESE;
		//����3��ֵ 0����������  1�����ݱ�ǩ 2��ǰn���ļ������ַ������ĺ�
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
		//���annԤ����
		Mat res=new Mat();
		//����
		for(int i=0; i<testData.rows(); i++) {
			//�ü������Լ���һ��
			Range rowRange=new Range(i, i+1);
			Range colRange=new Range(0, testData.cols());
			Mat temp=new Mat(testData, rowRange, colRange);
			//��Ŷ�����ͼ���Ԥ����
			Mat result=new Mat();
			//ʹ��annģ�ͽ���Ԥ���ʱ���������һ��ͼ���������Mat���͵ģ����Լ����յ�Ԥ����������������ʽ������ģ���е�ÿ����𶼸�����һ�����յ�Ԥ����������ȡ����Ԥ��ֵ��Ϊ�������յ�Ԥ����
			ann.predict(temp, result, 0);
			//��Ԥ������ӵ��ܼ���
			res.push_back(result);
		}
		//����ann��׼ȷ��
		int count=0;
		int index=0;
		//��ann���ж��У����յ��ж��Ľ����
		for(int cla=0; cla<nums.size(); cla++) {
			int t=(int) nums.get(cla);

			//��ͬһ�����ݼ��б���
			for(; index<t; index++) {
				//����������Ԥ���������λ��
				int position=0;
				double max=-2;
				for(int k=0; k<res.cols(); k++) {
					double tmp=res.get(index, k)[0];
					if(tmp>max) {
						max=tmp;
						position=k;
					}
				}
				//�ж�Ԥ��ֵ����λ���Ƿ�ͱ�ǩλ����ͬ
				if(position==cla) {
					count++;
				}
			}

		}
		System.out.println("�ж���ȷcount:"+count);
		System.out.println("����ĿtestData: "+testData.rows());
		System.out.println("׼ȷ�ʣ�"+count*1.0/testData.rows());
//		//���Ԥ������
//		for(int i1=0; i1<res.rows(); i1++) {
//			for(int j=0; j<res.cols(); j++) {
//				System.out.print(res.get(i1, j)[0]+"\t");
//			}
//			System.out.println();
//		}
	}
}
