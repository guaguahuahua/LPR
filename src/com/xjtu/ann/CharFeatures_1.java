package com.xjtu.ann;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class CharFeatures_1 {
	
	/**
	 * ʹ��easyPRԴ���и������㷨����������ȡ
	 * @param mat ԭͼ��ĻҶ�ͼ
	 * @return Mat
	 */
	public static Mat charFeatures_1(Mat mat) {
		if(mat.channels()!=1) {
			Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
		}
//		Imshow.imshow(mat);
		//���ص�����
		Mat features=new Mat(1, 120, CvType.CV_32FC1);
		//��òü�����
		Rect rect=getRect(mat);
		//����������жϣ���Ϊ��ֻ��һ�л�����һ�е������£��ᵼ�²ü��������ԭ���򣬴Ӷ�����Խ�����
		if(rect.width>mat.size().width || rect.height>mat.size().height) {
			rect.width=(int) mat.size().width;
			rect.height=(int) mat.size().height;
		}
//		System.out.println(rect.x+", "+rect.y+", "+rect.height+", "+rect.width);
		Mat area=cutRect(rect, mat);
		//�Բü�������д�С��һ��10*10
		Imgproc.resize(area, area, new Size(10, 10));
		//ˮƽ����ֱ�������ͶӰ
		double []vertical  =project(area, 0);
		double []horizontal=project(area, 1);
		//��ˮƽ��ֱ����д�뵽����������ȥ
		int index=0;
		for(int i=0; i<vertical.length; i++) {
			features.put(0, index++, new float[] {(float) vertical[i]});
		}
		for(int i=0; i<horizontal.length; i++) {
			features.put(0, index++, new float[] {(float) horizontal[i]});
		}
		//�������ص�ԭͼֱ����ӽ�����������ȥ
		for(int row=0; row<area.rows(); row++) {
			for(int col=0; col<area.cols(); col++) {
				features.put(0, index++, new float[] {(float) area.get(row, col)[0]});
			}
		}
		return features;
	}
	
	/**
	 * ��ȡ�ַ���������Ӿ���
	 * @param mat �Ҷ�ͼ
	 * @return ����
	 */
	private static Rect getRect(Mat mat) {
		
		//ȷ�����ε����±߽�
		int top=0;
		int bottom=mat.rows();
		boolean flag=false;
		for(int row=0; row<mat.rows(); row++) {
			for(int col=0; col<mat.cols(); col++) {
				if(mat.get(row, col)[0]>20) {
					top=row;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		flag=false;
		//�±߽�
		for(int row=mat.rows()-1; row>-1; row--) {
			for(int col=0; col<mat.cols(); col++) {
				if(mat.get(row, col)[0]>20) {
					bottom=row;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		flag=false;
		//���ұ߽�
		int left=0;
		int right=mat.cols();
		for(int col=0; col<mat.cols(); col++) {
			for(int row=0; row<mat.rows(); row++) {
				if(mat.get(row, col)[0]>20) {
					left=col;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		flag=false;
		//�ұ߽�
		for(int col=mat.cols()-1; col>-1; col--) {
			for(int row=0; row<mat.rows(); row++) {
				if(mat.get(row, col)[0]>20) {
					right=col;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		
		//��þ��β���
		int x=left;
		int y=top;
		int width=right-left+1;
		int height=bottom-top+1;
		Rect rect=new Rect(x, y, width, height);
		return rect;
	}
	
	/**
	 * ���ַ������ԭmat����ü�����
	 * @param rect Ҫ�ü�������
	 * @param mat ԭͼ
	 * @return Mat
	 */
	private static Mat cutRect(Rect rect, Mat mat) {
		//����һ�����ص�mat,���ü������ͼ�����θ��ƹ���
		Mat cutted=new Mat(mat, rect);	
		return cutted;
	}
	
	/**
	 * ���շ���ͶӰ��ˮƽ����Ϊ1����ֱ����Ϊ0
	 * @param mat
	 * @param direction
	 * @return
	 */
	public static double[] project(Mat mat, int direction) {
		//��¼����,��Ϊͼ����20*20 �������Σ�����ʹ���л����й涨��������������
		double []features=new double[mat.rows()];
		//��¼������ŵ�λ��
		int index=0;
		//��ű���������ı���
		int end=0;
		if(direction==0) {
			end=mat.cols(); //ˮƽ����
		}else {
			end=mat.rows(); //��ֱ����
		}
		int max=0;
		//���շ���ͶӰ
		for(int i=0; i<end; i++) {
			Mat data= (direction==0) ? mat.col(i) : mat.row(i);
			int featI=countObject(data);
			//ȡˮƽ���ߴ�ֱ�����ͶӰ�е����ֵ
			max=featI > max ? featI : max; 
			features[index++]=featI;
		}		
		//���
		if(max>0) {
			for(int i=0; i<features.length; i++) {
				features[i]=features[i] / max;
			}
		}
		return features;
	}
	
	/**
	 * ����һ�л�����һ���зǱ���Ԫ�صĸ���
	 * @param data һ�л�����һ��Ԫ��
	 * @return int
	 */
	public static int countObject(Mat data) {
		int count=0;
		if(data.cols()==1) { //������һ������
			//������һ����������Ǳ�������ôcount++
			for(int i=0; i<data.rows(); i++) {
				if(data.get(i, 0)[0]!=0) {
					count++;
				}
			}
		}else if(data.rows()==1){
			//����һ�У��Ǳ���Ԫcount++
			for(int i=0; i<data.cols(); i++) {
				if(data.get(0, i)[0]!=0) {
					count++;
				}
			}
		}		
		return count;
	}
	
	
	
	public static void main(String []args) {
		System.load("C:\\opencv-3.3.0\\build\\java\\x64\\opencv_java330.dll");
		//��������ͼ���λ��
		String file = "E:\\ann\\chars\\2\\201-2.jpg";
		Mat img=Imgcodecs.imread(file);
//		Mat grayImg=new Mat();
//		Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_RGB2GRAY);
		Imshow.imshow(img);
		Mat cutted=charFeatures_1(img);		
	}
}
