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
	//这块是加载文字分类器的模型ANN
	private static String ANNModelPath="E:/Eclipse/LPR/charsANN.xml";
	
	/**
	 * MSER文字定位，想法来自于easyPR，但是实现的过程主要是借助了opencv原生的MSER
	 * 过程处理使用了反复的投影裁剪的过程，而非种子生长法
	 * @param original	Mat	输入的原始图像
	 * @param results	List	临时的变量，在整合过程的之前有一定的作用，模块整合完毕，没有实际意义
	 * @param debug	boolean	是否进入调试模式
	 * @return	Mat	返回最终定位的结果
	 */
	public static Mat mserLocate(Mat original, List<Rect> results, boolean debug){	
		if(debug) {
			System.out.println("原始图像的大小："+original.size()+", "+original.rows()+", "+original.cols());
			Imshow.imshow(original, "原始图像.MserLocate");
		}		
		//下面使用的都是MSER方法中的常量参数，可以查看其C++层的接口
		final int imageArea=original.rows()*original.cols();
		final int delta=1;
		final int minArea=30;
		final double maxAreaRatio=0.05;
		MSER mser=MSER.create(delta, minArea, (int)(imageArea*maxAreaRatio), 0.25, 0.2, 200, 1.01, 0.003, 5);
		//图像进行灰度化
		Mat gray=new Mat();
		Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);
		//进行直方图均衡化
		Mat equalizedMat=new Mat();
		Imgproc.equalizeHist(gray, equalizedMat);
		if(debug) {
			//将均衡化前后的结果进行对比
			Imshow.imshow(gray, "原始的灰度图.MserLocate");
			Imshow.imshow(equalizedMat, "直方图均衡化图.MserLocate");
		}
		//获得灰度图像和原图像的备份，主要是后面展示使用
		Mat grayCopy=gray.clone();
		Mat originalCopy=original.clone();
		//加载ann模型，作为文字分类器
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
		
		//检测出区域
		List<MatOfPoint> pts=new ArrayList<MatOfPoint>();
		MatOfRect bboxes = new MatOfRect();
		mser.detectRegions(equalizedMat, pts, bboxes);
		//System.out.println("mser 提取出来区域的个数:"+pts.size());
		
		//计算面积
		Map<Rect, Double> area=new HashMap<Rect, Double>();
		//存放经过NMS处理过后的rect
		List <Rect>afterNMS=new ArrayList<Rect>();
		List <Double>scoreAfterNMS=new ArrayList<Double>();
		//为了方便后面nms处理，这块使用List将map中的信息提取出来,存放矩形框信息
		ArrayList<Rect> ll=new ArrayList<Rect>();
		//同样的使用ArrayList 对得分进行存放，与Rect最终的信息保持一致
		ArrayList<Double> processedScore=new ArrayList<Double>();
		//使用Rect数组记录所有的矩形区域
		Rect[] rectArray=new Rect[pts.size()];
		//使用double数组记录每个rect的得分
		double []score=new double[pts.size()];
		
		//标记这些区域
		for(int i=0; i<pts.size(); i++) {
			//需要获得外接矩形而不是旋转矩形，因为在进行NMS的过程中是要用到矩形的左上角和右下角的坐标才可以
			Rect rect=Imgproc.boundingRect(pts.get(i));
			//现在需要裁剪rect，并将这些rect抽取ann特征之后使用ann进行判定
			Mat temp=new Mat(gray, rect);
			//Imshow.imshow(temp);
			//System.out.println(temp.size()+", "+temp.rows()+", "+temp.cols());
			//System.out.println("第 "+i+" 个出现问题");
			//抽取字符的特征
			Mat feature=CharFeatures_1.charFeatures_1(temp);
			//使用ANN模型进行预测
			Mat preRes=new Mat();
			ann.predict(feature, preRes, 1);	//ann模型这里使用的flag的意义和作用是什么？
			//寻找预测概率最大的类,同时也得到了该矩形的得分
			double max=-2;
			for(int j=0; j<preRes.cols(); j++) {
				if(max<preRes.get(0, j)[0]) {
					max=preRes.get(0, j)[0];
				}
			}
			//将转化后rect存放到map中，并对每个map中的元素按照面积进行排序
			//2个参数分别是矩形框和对应的得分
			area.put(rect, max); 		
			results.add(rect);
		}
		
		//上面是对所有的元素放置到map当中去，下面段代码是实现对map中的集合按照得分进行排序
		//下面是对map中的元素进行排序的过程（升序排列）
		List<Map.Entry<Rect, Double>> list=new ArrayList<Map.Entry<Rect, Double>>(area.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Rect, Double>>() {
			@Override
			public int compare(Entry<Rect, Double> arg0, Entry<Rect, Double> arg1) {
				// TODO Auto-generated method stub
				return arg0.getValue().compareTo(arg1.getValue());
			}
		});

		//输出map中的key-value判断是否已经进行过了排序
		for(Map.Entry<Rect, Double> mapping: list) {
			//将已经有序的rect对象放置到ll当中，对应的将排好序得分值也存放在动态数组中主要的是方便后面的合并处理
			ll.add(mapping.getKey());
			processedScore.add(mapping.getValue());
		}
		
//		//对矩形框的得分进行排序
//		quickSort(rectArray, score, 0, score.length-1);
//		ArrayList<Rect> rectList=new ArrayList<Rect>();
//		ArrayList<Double> scoreList=new ArrayList<Double>();
//
//		//输出一下手写的排序算法的效果
//		for(int i=0; i<score.length; i++) {
//			System.out.println(rectArray[i]+", "+score[i]);
//			rectList.add(rectArray[i]);
//			scoreList.add(score[i]);
//		}
//		List<Rect> rectAfterNMS=new ArrayList<Rect>();
//		List<Double> scoreAfterNMS=new ArrayList<Double>();
//		NMS(rectList, scoreList, rectAfterNMS, scoreAfterNMS);
		
		//这块要做的是对所有的矩形框进行一个NMS过程
		//NMS code
		//NMS(ll, afterNMS);
		NMS(ll, processedScore, afterNMS, scoreAfterNMS);
		//这块是两个存放面积阈值过滤器需要的参数
		List<Rect> filteredArea=new ArrayList<Rect>();
		List<Double> filteredScore=new ArrayList<Double>();
		//对NMS处理过的区域进行面积阈值过滤
		filter(afterNMS, scoreAfterNMS, filteredArea, filteredScore);
		System.out.println("mserLocate 最终处理过的信息："+filteredArea.size()+", "+filteredScore.size());
		//如果经过了处理之后的区域个数为0，那么终止执行
		if(filteredArea.size()==0) {
			System.out.println("MSER定位失败，MserLocate.java");
			return null;
		}
		//对最终的面积和对应的阈值进行一个排序，使用快速排序算法
		Rect []areas=rectListToArray(filteredArea);
		double []scores=scoreListToArray(filteredScore); 
		quickSort(areas, scores, 0, scores.length-1);
		//根据字符位置，对图像进行裁剪
		Mat cutted=cut(areas, scores, originalCopy);
		//进行水平方向的投影并裁剪,得到最终的图像
		Mat res=project(cutted);
		//在原图上面绘制MSER得到的所有的结果
		for(int i=0; i<results.size(); i++) {
			DrawBoundingRectangle.drawBoundingRectangle_1(original, results);
		}
		
		//在二值图上面绘制NMS处理过的结果
		for(int i=0; i<afterNMS.size(); i++) {
			//Imgproc.drawContours(original, pts, i,new Scalar(0,0,255),1);
			DrawBoundingRectangle.drawBoundingRectangle_1(gray, afterNMS);
		}
		
		if(debug) {
			//这块是输出一下NMS之后的矩形区域的信息
			for(int i=0; i<afterNMS.size(); i++) {
				System.out.println(afterNMS.get(i));
			}
		}		
		//在二值图上面绘制面积阈值过滤之后的结果
		for(int i=0; i<filteredArea.size(); i++) {
			//输出过滤之后的面积
			//System.out.println(filteredArea.get(i).size()+", "+filteredArea.get(i).height+", "+filteredArea.get(i).width+", "+filteredScore.get(i));
			DrawBoundingRectangle.drawBoundingRectangle_1(grayCopy, filteredArea);
		}
		
//		//这块是对矩形长宽阈值过滤过的结果进行排序后进行展示
//		for(int i=0; i<areas.length; i++) {
//			System.out.println(areas[i]+", "+scores[i]);
//		}
		
		if(debug) {
			Imshow.imshow(original, "MSER定位得到文字区域.MserLocate");
			Imshow.imshow(gray, "afterNMS.MserLocate");
			Imshow.imshow(grayCopy, "字符定位最终结果.MserLocate");
			Imshow.imshow(cutted, "根据MSER裁剪出来的车牌区域.MserLocate");
		}		
		return res;
	}
	
	/**
	 * 根据文字定位的信息直接对图像裁剪，裁剪的条件是寻找得分高并且面积也大的矩形区域
	 * @param areas	Rect[]	矩形框区域	
	 * @param scores	double[]	每个矩形区域的得分
	 * @param mat	Mat	裁剪的原图像
	 * @return	Mat	裁剪的结果
	 */
	public static Mat cut(Rect []areas, double []scores, Mat mat) {
		//在得分大于0.7的矩形中寻找面积最大的矩形区域
		int max=Integer.MIN_VALUE;
		Rect rect=null;
		for(int i=scores.length-1; i>=0; i--) {
			Rect r=areas[i];
			if(scores[i]>0.8) {	
				//计算面积
				int s=r.width*r.height;
				if(s>max) {
					rect=r;
					max=s;
				}
			}else {
				break;
			}
		}
		//检查一下是否得到了合适的区域
		max=Integer.MIN_VALUE;
		//如果没有得分超过0.8的矩形区域，那么选择所有区域中最大的那个
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
//			if(scores[i]>0.8 && r.height*r.width>max) {	//这块的第二个项会导致提前终止判断，所以这个循环不是合理的
//				rect=r;
//				max=r.height*r.width;
//			}else {
//				break;
//			}
//		}
//		max=Integer.MIN_VALUE;
//		//如果没有得分超过0.8的矩形区域，那么选择所有区域中最大的那个
//		if(rect==null) {
//			for(int i=0; i<scores.length; i++) {
//				Rect r=areas[i];				
//				if(r.width*r.height>max) {
//					rect=r;
//					max=r.height*r.width;
//				}
//			}
//		}
//		System.out.println("最终的裁剪区域："+rect);
		//进入裁剪流程
		//创建裁剪区域
		int x=0;
		int y=rect.y;
		int width=mat.cols();
		int height=rect.height;
		Mat cutted=new Mat(mat, new Rect(x, y, width, height));
		return cutted;
	}
	

	/**
	 * 将水平裁剪过的图像进行垂直投影并裁剪最终的结果
	 * @param mat	Mat	水平裁剪过的图像
	 * @return	Mat	垂直裁剪
	 */
	public static Mat project(Mat mat) {
		//保留彩色图像的副本
		Mat matCopy=mat.clone();
		//将输入图像转化为灰度图
		Mat gray=new Mat();
		Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY);
//		Imshow.imshow(gray, "水平投影前的灰度图像");
		//将灰度图转化为二值图
		Mat bin=new Mat();
		Imgproc.threshold(gray, bin, Imgproc.THRESH_BINARY, 255, 0);
		Imgproc.adaptiveThreshold(gray, bin, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -20);//25,10，25，0
//		Imshow.imshow(bin, "水平投影.MserLocate");
		//获得水平投影效果
		int []project=new int[bin.cols()];
		//将每一列的不为0的像素值个数统计出来
		for(int i=0; i<bin.cols(); i++) {
			//获取这列的所有数据
			Mat colMat=bin.col(i);
			int count=0;
			//将这个数据进行一个统计
			for(int j=0; j<colMat.rows(); j++) {
				if((int) colMat.get(j, 0)[0]==255) {
					count++;
				}
			}
			project[i]=count;
		}
		//将投影结果输出
		for(int K:project) {
			System.out.print(K+"\t");
		}
		System.out.println();

		//确定裁剪的左右边界
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
		//裁剪
		int x=left;
		int y=0;
		int width=right-left+1;
		int height=mat.rows();
//		Mat projectMat=new Mat(bin, new Rect(x, y, width, height));
		Mat projectMat=new Mat(matCopy, new Rect(x, y, width, height));
//		Imshow.imshow(projectMat, "水平投影之后最终结果");
		return projectMat;
	}
	
	/**
	 * 将存放rect的list转化为rect数组
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
	 * 将存放得分的double list转化为double类型的数组
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
	 * 对NMS处理过的区域进行过滤，按照大小阈值来处理
	 * @param afterNMS	ArrayList	经过NMS处理过的
	 * @param processedScore	ArrayList	经过NMS处理过的矩形区的得分
	 * @param filteredArea	ArrayList	存放经过面积阈值过滤之后的区域的动态数组
	 * @param filteredScore	ArrayList	存放对应的经过面积阈值筛选过的得分
	 */
	public static void filter(List<Rect> afterNMS, List<Double> processedScore, List<Rect> filteredArea, List<Double>filteredScore) {
		//这是普通的字符在最终定位的结果中的平均的长和高
		double aspect=40.0D/80;
		//这是波动的范围
		double error=0.35;	
		//最小的长宽比
		double minAspect=0.05;	
		//最大的长宽比
		double maxAspect=aspect+aspect*error;	
		
		for(int i=0; i<afterNMS.size(); i++) {
			//获得一位置的得分和矩形区域
			Rect rect=afterNMS.get(i);
			double score=processedScore.get(i);
			//计算该矩形区的长宽比值
			double ratio=(double) rect.width/(double) rect.height;
			//如果该区域在面积范围内，那么我们将它添加到最后的结果集中
			if(ratio>=minAspect && ratio<=maxAspect ) {
				filteredArea.add(rect);
				filteredScore.add(score);
			}
		}
	}
	
	/**
	 * 使用快排的方式对得分进行排序，同时也对相应的rectArray进行排序
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
			//同时操作Rect数组，保持一致
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
			//操作Rect数组
			Rect tmp1=rectArray[r];
			rectArray[r]=rectArray[l];
			rectArray[l]=tmp1;
		}
		quickSort(rectArray, score, left, l);
		quickSort(rectArray, score, l+1, right);		
	}
	
	
	/**
	 * 使用NMS方法将多余的框去除掉
	 * @param ll	ArrayList集合，其中包括了rect以及他们的面积
	 * @param afterNMS	经过NMS处理过的Rect集合，只包含了那些没有重叠的部分
	 */
	public static void NMS(ArrayList<Rect> ll, List <Rect> afterNMS) {
		double overlap=0.6;
		while(!ll.isEmpty()) {
			//获得数组中的最后一个Rect
			Rect rect=ll.get(ll.size()-1);
			//依次的比较1~(n-1)和最后一个n的面积的交叉
			for(int i=0; i<ll.size()-1; i++) {
				//第一个矩形的左上角坐标和右下角坐标
				int x11=ll.get(i).x;
				int y11=ll.get(i).y;
				int x12=x11+ll.get(i).height;	//这块对于到底是给x+width 还是 x+height 这个问题目前是按照逻辑走的，有问题可以在这里修改
				int y12=y11+ll.get(i).width;
				//最后一个矩形（得分最高的矩形）的左上角坐标和右下角坐标
				int x21=rect.x;
				int y21=rect.y;
				int x22=x21+rect.height;
				int y22=y21+rect.width;
				//两个矩形求交的面积
				int intersect=(x12-x21)*(y12-y21);
				//求两个相交矩形中面积最小的那个
				int small=Integer.min(ll.get(i).width*ll.get(i).height, rect.width*rect.height);
				//求相交面积和两个矩形面积中最小的那个的比值
				double o=(double) intersect/small;
				//如果超过了给定的阈值，那么就抑制该矩形（第i个元素）
				if(o > overlap) {
					ll.remove(i);
				}
			}
			//将最后一个元素添加到结果集中
			afterNMS.add(rect);
			//将最后一个元素从开始给定的集合中移除
			ll.remove(rect);
		}
	}
	
	/**
	 * 重载上面的NMS过程，主要是使用了一个快排，保留了所有的数据项，现在在原来NMS基础上增加对得分也进行相应的处理，也就是将区域和得分
	 * 绑定在一起，同时处理
	 * @param rectArray	ArrayList	存放矩形区域的动态数组
	 * @param score	ArrayList	存放的分的动态数组 
	 * @param rectAfterNMS	List	存放经过NMS之后的矩形区域的动态数组
	 * @param scoreAfterNMS	List	存放经过NMS之后的得分的动态数组
	 */
	public static void NMS(ArrayList<Rect> rectArray, ArrayList<Double> score, List<Rect>rectAfterNMS, List<Double>scoreAfterNMS){
		double overlap=0.5;
		while(!rectArray.isEmpty()) {
			//获得数组中的最后一个Rect
			Rect rect=rectArray.get(rectArray.size()-1);
			//获得数组中最后一个Rect的得分
			double grade=score.get(score.size()-1);
			
			//依次的比较1~(n-1)和最后一个n的面积的交叉
			for(int i=0; i<rectArray.size()-1; i++) {
				//第一个矩形的左上角坐标和右下角坐标
				int x11=rectArray.get(i).x;
				int y11=rectArray.get(i).y;
				int x12=x11+rectArray.get(i).height;	//这块对于到底是给x+width 还是 x+height 这个问题目前是按照逻辑走的，有问题可以在这里修改
				int y12=y11+rectArray.get(i).width;
				//最后一个矩形（得分最高的矩形）的左上角坐标和右下角坐标
				int x21=rect.x;
				int y21=rect.y;
				int x22=x21+rect.height;
				int y22=y21+rect.width;
				//两个矩形求交的面积
				int intersect=(x12-x21)*(y12-y21);
				//求两个相交矩形中面积最小的那个
				int small=Integer.min(rectArray.get(i).width*rectArray.get(i).height, rect.width*rect.height);
				//求相交面积和两个矩形面积中最小的那个的比值
				double o=(double) intersect/small;
				//如果超过了给定的阈值，那么就抑制该矩形（第i个元素）
				if(o > overlap) {
					rectArray.remove(i);
					score.remove(i);
				}
			}
			//将最后一个元素添加到结果集中
			rectAfterNMS.add(rect);
			scoreAfterNMS.add(grade);
			//将最后一个元素从开始给定的集合中移除
			rectArray.remove(rect);
			score.remove(grade);
		}
	}
	
	
	public static void main(String []args) {
		System.load("C:\\opencv-3.3.0\\build\\java\\x64\\opencv_java330.dll");
		String file = "E:/CuttedImg/1 (2).jpg";	//实验图是针对第二张
		Mat original=Imgcodecs.imread(file);
//		Mat outputMat=new Mat();
		//用来存放定位到可能为车牌的区域
		List<Rect> results=new ArrayList<Rect>();
		boolean debug=false;
		mserLocate(original, results, debug);
	}
}
