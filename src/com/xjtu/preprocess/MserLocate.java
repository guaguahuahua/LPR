package com.xjtu.preprocess;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.features2d.MSER;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.ANN_MLP;

import com.xjtu.ann.CharFeatures_1;
import com.xjtu.ann.TrainANNModel;
import com.xjtu.util.DrawBoundingRectangle;
import com.xjtu.util.Imshow;

public class MserLocate {
	//����Ǽ������ַ�������ģ��ANN
	private static String ANNModelPath="E:/Eclipse/LPR/charsANN.xml";
	
	/**
	 * MSER���ֶ�λ���뷨������easyPR������ʵ�ֵĹ�����Ҫ�ǽ�����opencvԭ����MSER
	 * ���̴���ʹ���˷�����ͶӰ�ü��Ĺ��̣���������������
	 * @param original	Mat	�����ԭʼͼ��
	 * @param results	List	��ʱ�ı����������Ϲ��̵�֮ǰ��һ�������ã�ģ��������ϣ�û��ʵ������
	 * @param debug	boolean	�Ƿ�������ģʽ
	 * @return	Mat	�������ն�λ�Ľ��
	 */
	public static Mat mserLocate(Mat original, List<Rect> results, boolean debug){	
		if(debug) {
			System.out.println("ԭʼͼ��Ĵ�С��"+original.size()+", "+original.rows()+", "+original.cols());
			Imshow.imshow(original, "ԭʼͼ��.MserLocate");
		}		
		//����ʹ�õĶ���MSER�����еĳ������������Բ鿴��C++��Ľӿ�
		final int imageArea=original.rows()*original.cols();
		final int delta=1;
		final int minArea=30;
		final double maxAreaRatio=0.05;
		MSER mser=MSER.create(delta, minArea, (int)(imageArea*maxAreaRatio), 0.25, 0.2, 200, 1.01, 0.003, 5);
		//ͼ����лҶȻ�
		Mat gray=new Mat();
		Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);
		//����ֱ��ͼ���⻯
		Mat equalizedMat=new Mat();
		Imgproc.equalizeHist(gray, equalizedMat);
		if(debug) {
			//�����⻯ǰ��Ľ�����жԱ�
			Imshow.imshow(gray, "ԭʼ�ĻҶ�ͼ.MserLocate");
			Imshow.imshow(equalizedMat, "ֱ��ͼ���⻯ͼ.MserLocate");
		}
		//��ûҶ�ͼ���ԭͼ��ı��ݣ���Ҫ�Ǻ���չʾʹ��
		Mat grayCopy=gray.clone();
		Mat originalCopy=original.clone();
		//����annģ�ͣ���Ϊ���ַ�����
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
		
		//��������
		List<MatOfPoint> pts=new ArrayList<MatOfPoint>();
		MatOfRect bboxes = new MatOfRect();
		mser.detectRegions(equalizedMat, pts, bboxes);
		//System.out.println("mser ��ȡ��������ĸ���:"+pts.size());
		
		//�������
		Map<Rect, Double> area=new HashMap<Rect, Double>();
		//��ž���NMS��������rect
		List <Rect>afterNMS=new ArrayList<Rect>();
		List <Double>scoreAfterNMS=new ArrayList<Double>();
		//Ϊ�˷������nms�������ʹ��List��map�е���Ϣ��ȡ����,��ž��ο���Ϣ
		ArrayList<Rect> ll=new ArrayList<Rect>();
		//ͬ����ʹ��ArrayList �Ե÷ֽ��д�ţ���Rect���յ���Ϣ����һ��
		ArrayList<Double> processedScore=new ArrayList<Double>();
		//ʹ��Rect�����¼���еľ�������
		Rect[] rectArray=new Rect[pts.size()];
		//ʹ��double�����¼ÿ��rect�ĵ÷�
		double []score=new double[pts.size()];
		
		//�����Щ����
		for(int i=0; i<pts.size(); i++) {
			//��Ҫ�����Ӿ��ζ�������ת���Σ���Ϊ�ڽ���NMS�Ĺ�������Ҫ�õ����ε����ϽǺ����½ǵ�����ſ���
			Rect rect=Imgproc.boundingRect(pts.get(i));
			//������Ҫ�ü�rect��������Щrect��ȡann����֮��ʹ��ann�����ж�
			Mat temp=new Mat(gray, rect);
			//Imshow.imshow(temp);
			//System.out.println(temp.size()+", "+temp.rows()+", "+temp.cols());
			//System.out.println("�� "+i+" ����������");
			//��ȡ�ַ�������
			Mat feature=CharFeatures_1.charFeatures_1(temp);
			//ʹ��ANNģ�ͽ���Ԥ��
			Mat preRes=new Mat();
			ann.predict(feature, preRes, 1);	//annģ������ʹ�õ�flag�������������ʲô��
			//Ѱ��Ԥ�����������,ͬʱҲ�õ��˸þ��εĵ÷�
			double max=-2;
			for(int j=0; j<preRes.cols(); j++) {
				if(max<preRes.get(0, j)[0]) {
					max=preRes.get(0, j)[0];
				}
			}
			//��ת����rect��ŵ�map�У�����ÿ��map�е�Ԫ�ذ��������������
			//2�������ֱ��Ǿ��ο�Ͷ�Ӧ�ĵ÷�
			area.put(rect, max); 		
			results.add(rect);
		}
		
		//�����Ƕ����е�Ԫ�ط��õ�map����ȥ������δ�����ʵ�ֶ�map�еļ��ϰ��յ÷ֽ�������
		//�����Ƕ�map�е�Ԫ�ؽ�������Ĺ��̣��������У�
		List<Map.Entry<Rect, Double>> list=new ArrayList<Map.Entry<Rect, Double>>(area.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Rect, Double>>() {
			@Override
			public int compare(Entry<Rect, Double> arg0, Entry<Rect, Double> arg1) {
				// TODO Auto-generated method stub
				return arg0.getValue().compareTo(arg1.getValue());
			}
		});

		//���map�е�key-value�ж��Ƿ��Ѿ����й�������
		for(Map.Entry<Rect, Double> mapping: list) {
			//���Ѿ������rect������õ�ll���У���Ӧ�Ľ��ź���÷�ֵҲ����ڶ�̬��������Ҫ���Ƿ������ĺϲ�����
			ll.add(mapping.getKey());
			processedScore.add(mapping.getValue());
		}
		
//		//�Ծ��ο�ĵ÷ֽ�������
//		quickSort(rectArray, score, 0, score.length-1);
//		ArrayList<Rect> rectList=new ArrayList<Rect>();
//		ArrayList<Double> scoreList=new ArrayList<Double>();
//
//		//���һ����д�������㷨��Ч��
//		for(int i=0; i<score.length; i++) {
//			System.out.println(rectArray[i]+", "+score[i]);
//			rectList.add(rectArray[i]);
//			scoreList.add(score[i]);
//		}
//		List<Rect> rectAfterNMS=new ArrayList<Rect>();
//		List<Double> scoreAfterNMS=new ArrayList<Double>();
//		NMS(rectList, scoreList, rectAfterNMS, scoreAfterNMS);
		
		//���Ҫ�����Ƕ����еľ��ο����һ��NMS����
		//NMS code
		//NMS(ll, afterNMS);
		NMS(ll, processedScore, afterNMS, scoreAfterNMS);
		//�����������������ֵ��������Ҫ�Ĳ���
		List<Rect> filteredArea=new ArrayList<Rect>();
		List<Double> filteredScore=new ArrayList<Double>();
		//��NMS�������������������ֵ����
		filter(afterNMS, scoreAfterNMS, filteredArea, filteredScore);
		System.out.println("mserLocate ���մ��������Ϣ��"+filteredArea.size()+", "+filteredScore.size());
		//��������˴���֮����������Ϊ0����ô��ִֹ��
		if(filteredArea.size()==0) {
			System.out.println("MSER��λʧ�ܣ�MserLocate.java");
			return null;
		}
		//�����յ�����Ͷ�Ӧ����ֵ����һ������ʹ�ÿ��������㷨
		Rect []areas=rectListToArray(filteredArea);
		double []scores=scoreListToArray(filteredScore); 
		quickSort(areas, scores, 0, scores.length-1);
		//�����ַ�λ�ã���ͼ����вü�
		Mat cutted=cut(areas, scores, originalCopy);
		//����ˮƽ�����ͶӰ���ü�,�õ����յ�ͼ��
		Mat res=project(cutted);
		//��ԭͼ�������MSER�õ������еĽ��
		for(int i=0; i<results.size(); i++) {
			DrawBoundingRectangle.drawBoundingRectangle_1(original, results);
		}
		
		//�ڶ�ֵͼ�������NMS������Ľ��
		for(int i=0; i<afterNMS.size(); i++) {
			//Imgproc.drawContours(original, pts, i,new Scalar(0,0,255),1);
			DrawBoundingRectangle.drawBoundingRectangle_1(gray, afterNMS);
		}
		
		if(debug) {
			//��������һ��NMS֮��ľ����������Ϣ
			for(int i=0; i<afterNMS.size(); i++) {
				System.out.println(afterNMS.get(i));
			}
		}		
		//�ڶ�ֵͼ������������ֵ����֮��Ľ��
		for(int i=0; i<filteredArea.size(); i++) {
			//�������֮������
			//System.out.println(filteredArea.get(i).size()+", "+filteredArea.get(i).height+", "+filteredArea.get(i).width+", "+filteredScore.get(i));
			DrawBoundingRectangle.drawBoundingRectangle_1(grayCopy, filteredArea);
		}
		
//		//����ǶԾ��γ�����ֵ���˹��Ľ��������������չʾ
//		for(int i=0; i<areas.length; i++) {
//			System.out.println(areas[i]+", "+scores[i]);
//		}
		
		if(debug) {
			Imshow.imshow(original, "MSER��λ�õ���������.MserLocate");
			Imshow.imshow(gray, "afterNMS.MserLocate");
			Imshow.imshow(grayCopy, "�ַ���λ���ս��.MserLocate");
			Imshow.imshow(cutted, "����MSER�ü������ĳ�������.MserLocate");
		}		
		return res;
	}
	
	/**
	 * �������ֶ�λ����Ϣֱ�Ӷ�ͼ��ü����ü���������Ѱ�ҵ÷ָ߲������Ҳ��ľ�������
	 * @param areas	Rect[]	���ο�����	
	 * @param scores	double[]	ÿ����������ĵ÷�
	 * @param mat	Mat	�ü���ԭͼ��
	 * @return	Mat	�ü��Ľ��
	 */
	public static Mat cut(Rect []areas, double []scores, Mat mat) {
		//�ڵ÷ִ���0.7�ľ�����Ѱ��������ľ�������
		int max=Integer.MIN_VALUE;
		Rect rect=null;
		for(int i=scores.length-1; i>=0; i--) {
			Rect r=areas[i];
			if(scores[i]>0.8) {	
				//�������
				int s=r.width*r.height;
				if(s>max) {
					rect=r;
					max=s;
				}
			}else {
				break;
			}
		}
		//���һ���Ƿ�õ��˺��ʵ�����
		max=Integer.MIN_VALUE;
		//���û�е÷ֳ���0.8�ľ���������ôѡ�����������������Ǹ�
		if(rect==null) {
			for(int i=scores.length-1; i>=0; i--) {
				Rect r=areas[i];		
				int s=r.width*r.height;
				if(s>max) {
					rect=r;
					max=r.height*r.width;
				}
			}
		}
		
		
//		for(int i=0; i<scores.length; i++) {
//			Rect r=areas[i];
//			if(scores[i]>0.8 && r.height*r.width>max) {	//���ĵڶ�����ᵼ����ǰ��ֹ�жϣ��������ѭ�����Ǻ����
//				rect=r;
//				max=r.height*r.width;
//			}else {
//				break;
//			}
//		}
//		max=Integer.MIN_VALUE;
//		//���û�е÷ֳ���0.8�ľ���������ôѡ�����������������Ǹ�
//		if(rect==null) {
//			for(int i=0; i<scores.length; i++) {
//				Rect r=areas[i];				
//				if(r.width*r.height>max) {
//					rect=r;
//					max=r.height*r.width;
//				}
//			}
//		}
//		System.out.println("���յĲü�����"+rect);
		//����ü�����
		//�����ü�����
		int x=0;
		int y=rect.y;
		int width=mat.cols();
		int height=rect.height;
		Mat cutted=new Mat(mat, new Rect(x, y, width, height));
		return cutted;
	}
	

	/**
	 * ��ˮƽ�ü�����ͼ����д�ֱͶӰ���ü����յĽ��
	 * @param mat	Mat	ˮƽ�ü�����ͼ��
	 * @return	Mat	��ֱ�ü�
	 */
	public static Mat project(Mat mat) {
		//������ɫͼ��ĸ���
		Mat matCopy=mat.clone();
		//������ͼ��ת��Ϊ�Ҷ�ͼ
		Mat gray=new Mat();
		Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY);
//		Imshow.imshow(gray, "ˮƽͶӰǰ�ĻҶ�ͼ��");
		//���Ҷ�ͼת��Ϊ��ֵͼ
		Mat bin=new Mat();
		Imgproc.threshold(gray, bin, Imgproc.THRESH_BINARY, 255, 0);
		Imgproc.adaptiveThreshold(gray, bin, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -20);//25,10��25��0
//		Imshow.imshow(bin, "ˮƽͶӰ.MserLocate");
		//���ˮƽͶӰЧ��
		int []project=new int[bin.cols()];
		//��ÿһ�еĲ�Ϊ0������ֵ����ͳ�Ƴ���
		for(int i=0; i<bin.cols(); i++) {
			//��ȡ���е���������
			Mat colMat=bin.col(i);
			int count=0;
			//��������ݽ���һ��ͳ��
			for(int j=0; j<colMat.rows(); j++) {
				if((int) colMat.get(j, 0)[0]==255) {
					count++;
				}
			}
			project[i]=count;
		}
		//��ͶӰ������
		for(int K:project) {
			System.out.print(K+"\t");
		}
		System.out.println();

		//ȷ���ü������ұ߽�
		int left=0, right=project.length-1;
		boolean f1=false, f2=false;
		while(left<right) {
			while((left<right) && (project[left]<=10)) {
				left++;
			}
			f1=true;
			while((left<right) && (project[right]<=10)) {
				right--;
			}
			f2=true;
			if(f1 && f2) {
				break;
			}			
		}
		//�ü�
		int x=left;
		int y=0;
		int width=right-left+1;
		int height=mat.rows();
//		Mat projectMat=new Mat(bin, new Rect(x, y, width, height));
		Mat projectMat=new Mat(matCopy, new Rect(x, y, width, height));
//		Imshow.imshow(projectMat, "ˮƽͶӰ֮�����ս��");
		return projectMat;
	}
	
	/**
	 * �����rect��listת��Ϊrect����
	 * @param filteredArea	List<Rect>
	 * @return	Rect[]
	 */
	public static Rect [] rectListToArray(List<Rect> filteredArea) {
		Rect [] areas=new Rect[filteredArea.size()];
		for(int i=0; i<filteredArea.size(); i++) {
			areas[i]=filteredArea.get(i);
		}
		return areas;
	}
	
	/**
	 * ����ŵ÷ֵ�double listת��Ϊdouble���͵�����
	 * @param filteredScore	List<Double>
	 * @return	double[]
	 */
	public static double[] scoreListToArray(List<Double> filteredScore) {
		double []scores=new double[filteredScore.size()];
		for(int i=0; i<filteredScore.size(); i++) {
			scores[i]=filteredScore.get(i);
		}
		return scores;
	}
	
	
	/**
	 * ��NMS�������������й��ˣ����մ�С��ֵ������
	 * @param afterNMS	ArrayList	����NMS�������
	 * @param processedScore	ArrayList	����NMS������ľ������ĵ÷�
	 * @param filteredArea	ArrayList	��ž��������ֵ����֮�������Ķ�̬����
	 * @param filteredScore	ArrayList	��Ŷ�Ӧ�ľ��������ֵɸѡ���ĵ÷�
	 */
	public static void filter(List<Rect> afterNMS, List<Double> processedScore, List<Rect> filteredArea, List<Double>filteredScore) {
		//������ͨ���ַ������ն�λ�Ľ���е�ƽ���ĳ��͸�
		double aspect=40.0D/80;
		//���ǲ����ķ�Χ
		double error=0.35;	
		//��С�ĳ����
		double minAspect=0.05;	
		//���ĳ����
		double maxAspect=aspect+aspect*error;	
		
		for(int i=0; i<afterNMS.size(); i++) {
			//���һλ�õĵ÷ֺ;�������
			Rect rect=afterNMS.get(i);
			double score=processedScore.get(i);
			//����þ������ĳ����ֵ
			double ratio=(double) rect.width/(double) rect.height;
			//����������������Χ�ڣ���ô���ǽ�����ӵ����Ľ������
			if(ratio>=minAspect && ratio<=maxAspect ) {
				filteredArea.add(rect);
				filteredScore.add(score);
			}
		}
	}
	
	/**
	 * ʹ�ÿ��ŵķ�ʽ�Ե÷ֽ�������ͬʱҲ����Ӧ��rectArray��������
	 * @param rectArray	Rect[]
	 * @param score	double[]
	 */
	public static void quickSort(Rect []rectArray, double []score, int left, int right) {
		
		int l=left;
		int r=right;
		if(l>=r) {
			return ;
		}
		while(l<r) {
			while(l<r && score[l]<=score[r]) {
				r--;
			}
			if(l>r) {
				break;
			}
			double temp=score[l];
			score[l]=score[r];
			score[r]=temp;
			//ͬʱ����Rect���飬����һ��
			Rect temp1=rectArray[l];
			rectArray[l]=rectArray[r];
			rectArray[r]=temp1;
			
			while(l<r && score[l]<=score[r]) {
				l++;
			}
			if(l>r) {
				break;
			}
			double tmp=score[r];
			score[r]=score[l];
			score[l]=tmp;
			//����Rect����
			Rect tmp1=rectArray[r];
			rectArray[r]=rectArray[l];
			rectArray[l]=tmp1;
		}
		quickSort(rectArray, score, left, l);
		quickSort(rectArray, score, l+1, right);		
	}
	
	
	/**
	 * ʹ��NMS����������Ŀ�ȥ����
	 * @param ll	ArrayList���ϣ����а�����rect�Լ����ǵ����
	 * @param afterNMS	����NMS�������Rect���ϣ�ֻ��������Щû���ص��Ĳ���
	 */
	public static void NMS(ArrayList<Rect> ll, List <Rect> afterNMS) {
		double overlap=0.6;
		while(!ll.isEmpty()) {
			//��������е����һ��Rect
			Rect rect=ll.get(ll.size()-1);
			//���εıȽ�1~(n-1)�����һ��n������Ľ���
			for(int i=0; i<ll.size()-1; i++) {
				//��һ�����ε����Ͻ���������½�����
				int x11=ll.get(i).x;
				int y11=ll.get(i).y;
				int x12=x11+ll.get(i).height;	//�����ڵ����Ǹ�x+width ���� x+height �������Ŀǰ�ǰ����߼��ߵģ�����������������޸�
				int y12=y11+ll.get(i).width;
				//���һ�����Σ��÷���ߵľ��Σ������Ͻ���������½�����
				int x21=rect.x;
				int y21=rect.y;
				int x22=x21+rect.height;
				int y22=y21+rect.width;
				//���������󽻵����
				int intersect=(x12-x21)*(y12-y21);
				//�������ཻ�����������С���Ǹ�
				int small=Integer.min(ll.get(i).width*ll.get(i).height, rect.width*rect.height);
				//���ཻ��������������������С���Ǹ��ı�ֵ
				double o=(double) intersect/small;
				//��������˸�������ֵ����ô�����Ƹþ��Σ���i��Ԫ�أ�
				if(o > overlap) {
					ll.remove(i);
				}
			}
			//�����һ��Ԫ����ӵ��������
			afterNMS.add(rect);
			//�����һ��Ԫ�شӿ�ʼ�����ļ������Ƴ�
			ll.remove(rect);
		}
	}
	
	/**
	 * ���������NMS���̣���Ҫ��ʹ����һ�����ţ����������е������������ԭ��NMS���������ӶԵ÷�Ҳ������Ӧ�Ĵ���Ҳ���ǽ�����͵÷�
	 * ����һ��ͬʱ����
	 * @param rectArray	ArrayList	��ž�������Ķ�̬����
	 * @param score	ArrayList	��ŵķֵĶ�̬���� 
	 * @param rectAfterNMS	List	��ž���NMS֮��ľ�������Ķ�̬����
	 * @param scoreAfterNMS	List	��ž���NMS֮��ĵ÷ֵĶ�̬����
	 */
	public static void NMS(ArrayList<Rect> rectArray, ArrayList<Double> score, List<Rect>rectAfterNMS, List<Double>scoreAfterNMS){
		double overlap=0.5;
		while(!rectArray.isEmpty()) {
			//��������е����һ��Rect
			Rect rect=rectArray.get(rectArray.size()-1);
			//������������һ��Rect�ĵ÷�
			double grade=score.get(score.size()-1);
			
			//���εıȽ�1~(n-1)�����һ��n������Ľ���
			for(int i=0; i<rectArray.size()-1; i++) {
				//��һ�����ε����Ͻ���������½�����
				int x11=rectArray.get(i).x;
				int y11=rectArray.get(i).y;
				int x12=x11+rectArray.get(i).height;	//�����ڵ����Ǹ�x+width ���� x+height �������Ŀǰ�ǰ����߼��ߵģ�����������������޸�
				int y12=y11+rectArray.get(i).width;
				//���һ�����Σ��÷���ߵľ��Σ������Ͻ���������½�����
				int x21=rect.x;
				int y21=rect.y;
				int x22=x21+rect.height;
				int y22=y21+rect.width;
				//���������󽻵����
				int intersect=(x12-x21)*(y12-y21);
				//�������ཻ�����������С���Ǹ�
				int small=Integer.min(rectArray.get(i).width*rectArray.get(i).height, rect.width*rect.height);
				//���ཻ��������������������С���Ǹ��ı�ֵ
				double o=(double) intersect/small;
				//��������˸�������ֵ����ô�����Ƹþ��Σ���i��Ԫ�أ�
				if(o > overlap) {
					rectArray.remove(i);
					score.remove(i);
				}
			}
			//�����һ��Ԫ����ӵ��������
			rectAfterNMS.add(rect);
			scoreAfterNMS.add(grade);
			//�����һ��Ԫ�شӿ�ʼ�����ļ������Ƴ�
			rectArray.remove(rect);
			score.remove(grade);
		}
	}
	
	
	public static void main(String []args) {
		System.load("C:\\opencv-3.3.0\\build\\java\\x64\\opencv_java330.dll");
		String file = "E:/CuttedImg/1 (2).jpg";	//ʵ��ͼ����Եڶ���
		Mat original=Imgcodecs.imread(file);
//		Mat outputMat=new Mat();
		//������Ŷ�λ������Ϊ���Ƶ�����
		List<Rect> results=new ArrayList<Rect>();
		boolean debug=false;
		mserLocate(original, results, debug);
	}
}
