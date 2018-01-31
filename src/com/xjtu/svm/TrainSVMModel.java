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
	private final static int amount=400; //����ͼ����Ե�����
	private final static int trainNums=300; //ѵ��ͼ����Ե�����
	private static int width; //ѵ��ͼ��Ŀ�͸�
	private static int height;
	private static String fileName="E:/Eclipse/LPR/svm.xml"; //���svmģ�͵�·�����ļ�������

	/**
	 * �����г��Ƶ�ѵ����
	 * @param mat Mat
	 * 				ѵ��ͼ����������Mat����
	 * @param label List<Byte>
	 * 				ÿһ��ͼ���ǩ��Ӧ�ı�ǩ������������
	 * @param testImg Mat
	 * 				����ͼ�����������
	 * @param labels List<Byte>
	 * 				����ͼ���ǩ��ż���
	 */
	public static void readInPlateImg(Mat mat, List<Byte>label, Mat testImg, List<Byte>labels) {
		String path="E:/HasPlate";
		File file=new File(path);
		//��ø�·������������ļ�����������ļ�������
		File[] fileArray=file.listFiles();
		if(fileArray.length==0) {
			System.out.println("�ļ���Ϊ�գ�û��ͼ����룡");
		}
		//�������ͼ���list
		for(int i=0; i<amount; i++) {
			//����ļ��ľ���·��
			path=fileArray[i].getAbsolutePath();
			//����ͼ����mat �ĸ�ʽ����
			Mat tmp=Imgcodecs.imread(path);
			//����Ϊ����ϲ�����������׼��
			width=tmp.cols();
			height=tmp.rows();
			Mat feature=ExtractFeature.extractFeature(tmp);
			//����ͼ����ӵ���Ӧ��������ȥ
			if(i<trainNums) { //ǰ300��ͼ����ص�ѵ����
				mat.push_back(feature);
				label.add((byte)1);
			}else { //��100��ͼ����ز��Լ�
				testImg.push_back(feature);
				labels.add((byte)1);
			}
		}
	}
	
	/**
	 * �����޳��Ƶ�ѵ����
	 * @param mat Mat
	 * 				ѵ��ͼ��������ż���
	 * @param label List<Byte>
	 * 				ѵ��ͼ��ı�ǩ����
	 * @param testImg Mat
	 * 				����ͼ����������
	 * @param labels List<Byte>
	 * 				����ͼ��ı�ǩ����
	 */
	public static void readInNoPlateImg(Mat mat, List<Byte>label, Mat testImg, List<Byte>labels) {
		String path="E:/NoPlate";
		File file=new File(path);
		File[] fileArray=file.listFiles();
		if(fileArray.length==0) {
			System.out.println("�ļ���Ϊ�գ�û��ͼ����룡");
		}
		//�������ͼ���list
		for(int i=0; i<amount; i++) {
			//����ļ��ľ���·��
			path=fileArray[i].getAbsolutePath();
			//����ͼ����mat �ĸ�ʽ����
			Mat tmp=Imgcodecs.imread(path);
			Mat feature=ExtractFeature.extractFeature(tmp);
			//����ͼ����ӵ���Ӧ��������ȥ
			if(i<trainNums) {
				mat.push_back(feature); //ֱ�ӽ�һ�� 36*136 ��ͼ����ӵ�mat�������
				label.add((byte)0);
			}else {
				testImg.push_back(feature);
				labels.add((byte)0);
			}
		}
	}
	
	
	/**
	 * ѵ��SVM ����ѵ���õ�ģ��д��xml�ļ�
	 * @param trainningData Mat
	 * 						ѵ�����ݵ�������
	 * @param clusses Mat
	 * 						ѵ�����ݵı�ǩ��
	 * ע�� :
	 *    trainningData CV_32FC1
	 *    clusses       CV_32SC1
	 */
	public static void trainSVM(Mat trainningData, Mat clusses) {
		//���ݱ����opencv��Ҫ�����ݸ�ʽ,�������svm��ѵ����
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
						Ml.ROW_SAMPLE, //�������������д洢�����д洢
						clusses,
						10,
						SVM.getDefaultGridPtr(SVM.C),
						SVM.getDefaultGridPtr(SVM.GAMMA),
						SVM.getDefaultGridPtr(SVM.P),
						SVM.getDefaultGridPtr(SVM.NU),
						SVM.getDefaultGridPtr(SVM.COEF),
						SVM.getDefaultGridPtr(SVM.DEGREE),
						true);
				//��svmģ��д�뵽xml�ļ�
				svm.save(fileName);	
				System.out.println("SVM ģ��ѵ����ϣ�");
	}

	/**
	 * ���������������а����˱Ƚ���Ҫ��һЩ���֣�
	 * ����ת�����֣� 
	 * 	ѵ�����ݼ����������� CvType.CV_32FC1
	 *  ��ǩ���ݼ����������� CvType.CV_32SC1
	 * svm�ļ��ز��֣�
	 * 	��Ϊload(filePath)�����Ǹ�static�����������Լ���svm���Ҫ���صĻ�
	 *  ��Ҫ��SVM.load(filePath)���أ� �����ǴӶ���õ� svm.load(filePath)�м��أ�
	 *  �ڶ��ַ�ʽ���ػ᷵��һ���µĶ��󣬴Ӷ����´���
	 * @param args
	 */
	public static void main(String []args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//�����г��ƺ��޳��Ƶ�ͼ�񣬲������б��
		List<Byte> label=new ArrayList<Byte>();
		Mat trainningSet=new Mat();
		Mat trainningData=new Mat();
		//��ŷ����ǩ,��ǩ��������ѵ��ͼ�������Ŀ�����Գ�2
		Mat classes=new Mat(2*trainNums, 1, CvType.CV_8UC1); 
		Mat clusses=new Mat();
		//��Ų���ͼ��ͱ�ǩ��mat
		Mat testImg=new Mat(); 
		List<Byte> labels=new ArrayList<Byte>();
		//����·������ѵ������
		readInPlateImg(trainningSet, label, testImg, labels);
		readInNoPlateImg(trainningSet, label, testImg, labels);
		//��ͼƬ����ת��ΪCvSVMҪ������ݸ�ʽ
		trainningSet.convertTo(trainningData, CvType.CV_32FC1);
		//���ڽ�labelת��Ϊmat���ͣ���ͨ������Ϊû�к��ʵĽӿڣ������Լ�д
		for(int i=0; i<label.size(); i++) {
			//��ǩ�ǵ�ͨ����Mat������ֻ��һ�У���0��1���
			byte[] t=new byte[1];
			t[0]=label.get(i);
			classes.put(i, 0, t);
		}
		//����ǩ����ת��ΪCvSVMҪ������ݸ�ʽ
		classes.convertTo(clusses, CvType.CV_32SC1); //ע�����ͱ���ʹ��CV_32S����Ӧ������������
		System.out.println("clusses.size: "+clusses.size());
		//����svm.xml�ļ�
		File file=new File(fileName);
		//��ʼ��svm
		SVM svm=null;
		//�ж�ϵͳ��û��ѵ���õ�ģ���ļ�
		if(file.exists()) {
			//�����ѵ���õ�ģ���ļ�����ôֱ�Ӽ��ص�svm
			//��Ϊload��һ����̬�������������Ҫ����ǰ��svm����ѵ���õ�ģ�͵Ļ�����Ҫͨ���෽��
			svm=SVM.load(fileName);
		//���û��ѵ���õ�ģ���ļ�����ô����ϵͳ��svmֱ��ѵ��ģ�ͣ�����ѵ���õ�ģ�ͱ�����svm.xml��ȥ
		}else {
			System.out.println("SVM ģ�Ͳ����ڣ���ʼѵ��....");
			trainSVM(trainningData, clusses);
		}
		System.out.println("��ǩ���� ������"+labels.size());
		System.out.println("ѵ��������: "+trainningData.rows()+", ѵ������������: "+trainningData.cols());
		//ͳ��Ԥ������ԭ������ͬ�ĸ���
		int counting=0;
		//���������ݵ�Mat���м��أ� svm����һ��Ԥ��
		for(int row=0; row<testImg.rows(); row++) {
			//�Ӳ������ݼ��еõ�һ�У�����ͼ���ǳ�ȡ136+36ά������֮�����д洢�ģ�����ÿһ������������ʾһ��ͼ��
			//���к�������mat�м��г�һ�У�testImg��һ�о���һ��ͼ�����Բü�����һ��
			Mat p=new Mat(testImg, new Range(row, row+1), new Range(0,testImg.cols())); 
			//����������ת��ΪCvSVM����Ҫ�ĸ�ʽ
			p.convertTo(p, CvType.CV_32F);
			//���뵽ģ���н����жϣ���������һ���������ͣ� ���ǽ���ת��Ϊ����
			int svmP=(int)svm.predict(p); //svmģ���жϵ�ǰͼ�������
			//��ȡ�ж�ͼ��ı�ǩ
			int cla=labels.get(row);
			//����ж�����͸�ͼ��ı�ǩһ�£�������ΪԤ��ɹ�����������+1
			if(svmP==cla) {
				counting++;
			}
		}
		//�������Ԥ��׼ȷ��
		double rate=(double) counting/(double) labels.size();
//		System.out.println("C: "+svm.getC());
//		System.out.println("coef: "+svm.getCoef0());
//		System.out.println("degree: "+svm.getDegree());
//		System.out.println("gamma: "+svm.getGamma());
//		System.out.println("kernel: "+svm.getKernelType());
//		System.out.println("nu: "+svm.getNu());
//		System.out.println("p: "+svm.getP());
//		System.out.println("type: "+svm.getType());
		System.out.println("�ж�׼ȷͼ��"+counting+", "+"�ܹ�ͼ��������"+labels.size()+", �ж�׼ȷ�ʣ�"+rate);
	}
}
