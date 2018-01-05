package com.xjtu;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.imgcodecs.Imgcodecs;

import com.xjtu.charseperate.SeperateChars;
import com.xjtu.classify.AreaSelect;
import com.xjtu.classify.SVMClassify;
import com.xjtu.preprocess.ColorLocate;
import com.xjtu.preprocess.SobelLocate;
import com.xjtu.recognize.CharRecognize;
import com.xjtu.util.DeskewAndAffine;
import com.xjtu.util.Imshow;
import com.xjtu.util.Resize1;

public class Test {
	
		private static String returnWords="请重新拍照！";
		
		/**
		 * 这块是服务端处理流程的开始
		 * @param filePath 从服务端获取的图像在本地的存放绝对地址
		 * @return String 服务端处理图像最后的识别的结果
		 */
		public static String test(String filePath) {
			//加载opencv库，但是在tomcat服务器环境下需要使用绝对路径，即使用下面的未注释的语句
			//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			System.load("C:\\opencv-3.3.0\\build\\java\\x64\\opencv_java330.dll");
			//处理流程图像的位置
			String file = "E:/CuttedImg/1 (4).jpg";
			//下面的路径是从客户端获得的图像在服务端的存放位置
//			String file=filePath;
			//读入图像
			Mat original=Imgcodecs.imread(file);
			Mat outputMat=new Mat();
			//用来存放定位到可能为车牌的区域
			List<RotatedRect> results=new ArrayList<RotatedRect>();
			//使用sobel定位
			SobelLocate.sobelLocate(original,results);
			//在原图上面绘制外接矩形轮廓
			//DrawBoundingRectangle.drawBoundingRectangle(original, results);
			//存放倾斜矫正的之后的图像
			List<Mat> res=new ArrayList<Mat>();
			//参数依次为：原图，轮廓图，切割图
			DeskewAndAffine.deskewAndAffine(original, results, res);
			//使用颜色定位
			List<RotatedRect> colorRes=new ArrayList<RotatedRect>();
			ColorLocate.colorLocate(original, 1, colorRes);
			//这块应该调用倾斜矫正方法和分割方法，将满足轮廓阈值的区域进行分割和转正
			System.out.println("colorLocate:"+colorRes.size());
			//在原图上面绘制外接矩形轮廓
			//DrawBoundingRectangle.drawBoundingRectangle(original, colorRes);
			//Imshow.imshow(original);
			//存放倾斜矫正的之后的图像
			List<Mat> res1=new ArrayList<Mat>();
			//参数依次为：原图，轮廓图，切割图
			DeskewAndAffine.deskewAndAffine(original, colorRes, res1);
			//使用svm对待定区域进行判定
			List <Mat> pengdingImg=new ArrayList <Mat>();
			//将两种方式裁剪的图像放在同一个数组中
			pengdingImg.addAll(res1);
			pengdingImg.addAll(res);
			//调用SVM模型对待定区域进行分类
			List<Mat> classifiedImg=new ArrayList<Mat>();
			SVMClassify.svmClassify(pengdingImg, classifiedImg);
//			//展示分类过的图像
//			for(int i=0; i<classifiedImg.size(); i++) {
//				Imshow.imshow(classifiedImg.get(i));
//			}
//			System.out.println(classifiedImg.size());
			//这块是使用了一个蓝色面积阈值法来筛选车牌和非车牌区域
			Mat last=AreaSelect.areaSelect(classifiedImg);
			if(last!=null) {
//				Imshow.imshow(last);
			}else {
				System.out.println("定位失败或者是svm判定失败！ ");
				return returnWords;
			}
			//字符的切分
			//存放切分之后单个字符的图像
			List<Mat> charsImg=new ArrayList<Mat>(); 
			int check=SeperateChars.seperateChars(last, charsImg);
			if(check<0) {
				return returnWords;
			}
			//查看一下切分出来字符的大小
			System.out.println("查看一下切分出来字符的大小");
			//存放归一化后的字符图像
			List <Mat>resizedImgs=new ArrayList<Mat>();
			for(int i=0; i<charsImg.size(); i++) {
				Mat mat=Resize1.resize1(charsImg.get(i));
				resizedImgs.add(mat);
//				Imshow.imshow(mat);
			}
			//调用ANN对切分之后的字符进行识别，并产生输出
			String carString=CharRecognize.charRecognize(resizedImgs);
			System.out.println(carString);
			return carString;
		}
		
		public static void main(String[] args) {
			test("");
		}
}
