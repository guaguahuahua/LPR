package com.xjtu.ann;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class ChineseFeatures {
	//����ˮƽ�ʹ�ֱ����
	private static final int VERTICAL=1;
	private static final int HORIZONTAL=0;
	
	/**
	 * ��֤���ݽ�����ͼ���ǵ�ͨ���ĻҶ�ͼ
	 * ��ȡ�ַ�����
	 * @param grayImg �ַ��ĻҶ�ͼ
	 * @return ��ȡ������
	 */
	public static Mat chineseFeatures(Mat grayImg) {
		if(grayImg.channels()!=1) {
			Imgproc.cvtColor(grayImg, grayImg, Imgproc.COLOR_RGB2GRAY);
		}
		//���ȶ�ͼ����д�С��ת������
//		Imshow.imshow(grayImg);
		Mat resizedImg=new Mat();
		Rect rect=getRect(grayImg);
		Mat cuttedImg=cutRect(rect, grayImg);
//		Imshow.imshow(cuttedImg);
		//20*20--->32*20��ͼ��������ʹ�������Բ�ֵ�Ĺ�������󲿷ֵ�����
		Imgproc.resize(grayImg, resizedImg, new Size(20, 32), 0, 0, Imgproc.INTER_LINEAR);
		//����Ǹ�����ͶӰ������
		Mat mat=resizedImg.clone();
//		Imshow.imshow(mat);
		//
//		for(int row=0; row<resizedImg.rows(); row++) {
//			for(int col=0; col<resizedImg.cols(); col++) {
//				System.out.print(resizedImg.get(row, col)[0]+"\t");
//			}
//			System.out.println();
//		}
//		Imshow.imshow(mat);
		//���Ƚ�ͼ��ת��Ϊ�������͵�ͼ��
		Mat normImg=new Mat();
		resizedImg.convertTo(normImg, CvType.CV_32FC1);
		//�ԻҶ�ͼ���������ص���г���255����
		double alpha=1.f/255;
		Mat meanImg=new Mat();
		normImg.convertTo(meanImg, CvType.CV_32FC1, alpha);
		//��������ͼ��ľ�ֵ
		Scalar means=Core.mean(meanImg);
		//��ԭͼ�ϼ�������ͼ��ľ�ֵ
		for(int row=0; row<meanImg.rows(); row++) {
			for(int col=0; col<meanImg.cols(); col++) {
				//���ԭͼ���ڸ�λ�õ�ֵ�������Ԫ����ֵ���ֵ�Ĳ�
				double diff=meanImg.get(row, col)[0]-means.val[0];
				//�����ǵĲ�ֵ��Ϊ������뵽ԭͼ����
				meanImg.put(row, col, new float[] { (float) diff });				
			}
		}		
		//��ԭͼ����������Ϊ1*640ά����,��һ��������ͨ����Ŀ�����Ϊ0��ʾ��ԭ����ͨ����һ����
		Mat ori=meanImg.reshape(1, 1);
		//��ԭͼ���ֵ��
		Mat dst=new Mat();
		Imgproc.threshold(mat, dst, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY, 255, 0);
//		Imshow.imshow(mat);
//		Imshow.imshow(dst);
		//����ֵͼ�ı��СΪ32*32
		Imgproc.resize(dst, dst, new Size(32, 32), 0, 0, Imgproc.INTER_LINEAR);
		//����ˮƽ��ֱͶӰ
		Mat vertical=project(dst, 0);
		Mat horizontal=project(dst, 1);	
		
		//������󷵻ص�����
		Mat feature=new Mat(1, (horizontal.cols()+vertical.cols()) + ori.cols(),  CvType.CV_32FC1);
		//���εĽ���ȡ���������������ķ�����������
		int index=0;
		for(int i=0; i<vertical.cols(); i++) {
			float val=(float) vertical.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
		for(int i=0; i<horizontal.cols(); i++) {
			float val=(float) horizontal.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
				
		for(int i=0; i<ori.cols(); i++) {
			float val=(float) ori.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
		return feature;
	}
	
	
	/**
	 * ��ȡͼ���gabor�������ƻ���4�����򣬹�4*20*20=1600ά����
	 * @param grayImg �Ҷ�ͼ
	 * @return Mat
	 */
	public static Mat gaborFeatures(Mat grayImg) {
		Mat features=new Mat(1, 400*4, CvType.CV_32FC1);
		Rect rect=getRect(grayImg);
		Mat cutted=cutRect(rect, grayImg);
//		Imshow.imshow(cutted);
		Mat resized=new Mat();
		Imgproc.resize(cutted, resized, new Size(20, 20));
		//��ȡgabor��
		Mat gaborKernal=Imgproc.getGaborKernel(new Size(4, 4), Math.PI, 0, 2*Math.PI/3.3, 1);
		Mat dst=new Mat();
		Imgproc.filter2D(resized, dst, -1, gaborKernal);
		Mat gaborKernal1=Imgproc.getGaborKernel(new Size(4, 4), Math.PI, Math.PI/4, 2*Math.PI/3.3, 1);
		Mat second=new Mat();
		Imgproc.filter2D(resized, second, -1, gaborKernal1);
		Mat gaborKernal2=Imgproc.getGaborKernel(new Size(4, 4), Math.PI, Math.PI/2, 2*Math.PI/3.3, 1);
		Mat third=new Mat();
		Imgproc.filter2D(resized, third, -1, gaborKernal2);
		Mat gaborKernal3=Imgproc.getGaborKernel(new Size(4, 4), Math.PI, 3*Math.PI/4, 2*Math.PI/3.3, 1);
		Mat forth=new Mat();
		Imgproc.filter2D(resized, forth, -1, gaborKernal3);
		int index=0;
		//��20*20��׼���������뵽����������ȥ
		for(int row=0; row<dst.rows(); row++) {
			for(int col=0; col<dst.cols(); col++) {
				double d=dst.get(row, col)[0];
				features.put(0, index++, new float[] { (float) d/255 });
			}
		}
		for(int row=0; row<second.rows(); row++) {
			for(int col=0; col<second.cols(); col++) {
				double d=second.get(row, col)[0];
				features.put(0, index++, new float[] { (float) d/255 });
			}
		}
		for(int row=0; row<third.rows(); row++) {
			for(int col=0; col<third.cols(); col++) {
				double d=third.get(row, col)[0];
				features.put(0, index++, new float[] { (float) d/255 });
			}
		}
		for(int row=0; row<forth.rows(); row++) {
			for(int col=0; col<forth.cols(); col++) {
				double d=forth.get(row, col)[0];
				features.put(0, index++, new float[] { (float) d/255 });
			}
		}
		//�������ͶӰ����
//		Mat h=project(resized, 0);
//		Mat v=project(resized, 1);
//		for(int i=0; i<h.cols(); i++) {
//			double d=h.get(0, i)[0];
//			features.put(0, index++, new float[] { (float) d });
//		}
//		for(int i=0; i<v.cols(); i++) {
//			double d=v.get(0, i)[0];
//			features.put(0, index++, new float[] {(float) d});
//		}
		
		
		//
//		for(int i=0; i<features.cols(); i++) {
//			System.out.print(features.get(0, i)[0]+"\t");
//		}
//		Imshow.imshow(dst);
		return features;
	}
	/**
	 * ʹ��LBP��ͶӰ����
	 * ����1��ֱ��ʹ��lbp������ͶӰֱ��ͼ�����Եõ���lbp�����������һ���Ҷ�ͳ�ƣ�ÿ���Ҷ�ֵ��Ƶ����Ϊ������������256���������������ˮƽ��ֱ�����ͶӰ
	 * ����2���õ�lbp��������֮�󣬶������ҶȾ�������һ������֮�󣨳�255����ֱ������Ϊ1ά��������Ϊ�������ˮƽ��ֱͶӰ
	 * ����3����lbp��������32*32���зֿ飬��Ϊ4*4��С�飬��ÿ��С�����ˮƽ��ֱͶӰ
	 * @param grayImg
	 * @return Mat
	 */
	public static Mat LBPAndProjectFeatures(Mat grayImg) {
		if(grayImg.channels()!=1) {
			Imgproc.cvtColor(grayImg, grayImg, Imgproc.COLOR_RGB2GRAY);
		}
		//���ȶ�ͼ����д�С��ת������
		Mat resizedImg=new Mat();
		Rect rect=getRect(grayImg);
		Mat cuttedImg=cutRect(rect, grayImg);
//		Imshow.imshow(cuttedImg);
		//20*20--->32*20��ͼ��������ʹ�������Բ�ֵ�Ĺ�������󲿷ֵ�����
		Imgproc.resize(grayImg, resizedImg, new Size(32, 32), 0, 0, Imgproc.INTER_LINEAR);
		//����Ǹ�����ͶӰ������
		Mat mat=resizedImg.clone();
		//��ȡͼ��LBP����
		int radius=1;
		int neighbor=8;
		Mat lbp=new Mat(resizedImg.rows(), resizedImg.cols(), CvType.CV_32FC1);
		elbp(resizedImg, lbp, radius, neighbor);
		int indice=0;
		//�����Ҫ�Ƿ���������С����һ��64����ÿ��������ȡ8������
		float []pFeature=new float[64*8];
		for(int i=0; i<8; i++) {
			for(int j=0; j<8; j++) {
				//�ü�4*4С����,������ˮƽ�ʹ�ֱ�����ͶӰ
				Mat temp=new Mat(lbp, new Range(4*i, 4*i+4), new Range(4*j, 4*j+4));
				Mat h=project(temp, HORIZONTAL);
				Mat v=project(temp, VERTICAL);
				//��ˮƽ�ʹ�ֱ�����ͶӰ�����������
				for(int k=0; k<h.cols(); k++) {
					double t=h.get(0, k)[0];
					pFeature[indice++]=(float) t;
				}
				for(int k=0; k<v.cols(); k++) {
					double t=v.get(0, k)[0];
					pFeature[indice++]=(float) t;
				}
			}
		}
		//���һ�³�ȡ���µ�����
//		for(int i=0; i<pFeature.length; i++) {
//			System.out.print(pFeature[i]+"\t");
//		}
		
		
//		lbp.convertTo(lbp, CvType.CV_8UC1); //���ת����Ҫ��Ϊ����ʾͼ��
//		Imshow.imshow(lbp);
		//����ͳ��lbp�����س��ֵ�Ƶ��
		//��lbp��������һ��������һ��
//		for(int row=0; row<lbp.rows(); row++) {
//			for(int col=0; col<lbp.cols(); col++) {
////				table[(int) lbp.get(row, col)[0]]++;
//				double val=lbp.get(row, col)[0];
//				lbp.put(row, col, new float[] {(float) ( val/255 )});
//			}
//		}
		//����Ϊһ������
//		Mat reshaped=lbp.reshape(1, 1);
//		int max=Integer.MIN_VALUE;
//		//�ҵ�Ƶ������
//		for(int i=0; i<table.length; i++) {
//			if(table[i]>max) {
//				max=table[i];
//			}
//		}
//		float []features=new float[256];
//		//��һ��,�õ�����
//		if(max>0) {
//			for(int i=0; i<table.length; i++) {
//				features[i]=(float) table[i]/max;
//			}
//		}
		//��ԭͼ���ֵ��
		Mat dst=new Mat();
		Imgproc.threshold(mat, dst, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY, 255, 0);

		
		//����ֵͼ�ı��СΪ32*32
		Imgproc.resize(dst, dst, new Size(32, 32), 0, 0, Imgproc.INTER_LINEAR);
		//����ˮƽ��ֱͶӰ
		Mat vertical=project(dst, 0);
		Mat horizontal=project(dst, 1);	
		
		//������󷵻ص�����
		Mat feature=new Mat(1, (horizontal.cols()+vertical.cols()+pFeature.length) ,  CvType.CV_32FC1);
		//���εĽ���ȡ���������������ķ�����������
		int index=0;
		for(int i=0; i<vertical.cols(); i++) {
			float val=(float) vertical.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
		for(int i=0; i<horizontal.cols(); i++) {
			float val=(float) horizontal.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
		for(int i=0; i<pFeature.length; i++) {
			feature.put(0, index++, new float[] {pFeature[i]});
		}
		
//		for(int i=0; i<features.length; i++) {
//			feature.put(0, index++, new float[] { features[i] });
//		}		
		
//		for(int i=0; i<reshaped.cols(); i++) {
//			float val=(float) reshaped.get(0, i)[0];
//			feature.put(0, index++, new float[] { val });
//		}
		return feature;
	}
	/**
	 * ˮƽ�ʹ�ֱ�������ͶӰ������ͶӰ���й��
	 * @param mat ԭͼ��
	 * @param direction ͶӰ����, �涨0Ϊˮƽ��1Ϊ��ֱ
	 * @return double[]
	 */
	public static Mat project(Mat mat, int direction) {
		//��¼�����������������Ĵ�С��������
		int col=direction==0 ? mat.cols() : mat.rows();
		Mat features=new Mat(1, col, CvType.CV_32FC1);
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
			features.put(0, i, new float[] { featI });
		}		
		//���
		if(max>0) {
			for(int i=0; i<features.cols(); i++) {
				double val=features.get(0, i)[0] / max;
				features.put(0, i, new float[] { (float) val});
			}
		}
		return features;
	}
	
	/**
	 * ���ʹ�ûҶ�����ķ�ʽ����Ϊͳ��������������ÿһ�еĶ����ĸ���
	 * @param mat ��ֵ��ͼ��
	 * @param direction ͶӰ�ķ���0��ʾˮƽ��1��ʾ��ֱ
	 * @return Mat
	 */
	private static Mat project_1(Mat mat, int direction) {
		//�õ�������������
		int col= direction==0 ? mat.cols() : mat.rows();
		//������ŷ��صĶ���
		Mat features=new Mat(1, col, CvType.CV_32FC1);
		int index=0;
		int max=0;
		//���շ���ͶӰ
		for(int i=0; i<col; i++) {
			//��ȡĳһ���л�����ĳһ��
			Mat data= direction==0 ? mat.col(i) : mat.row(i);
			//ͳ����һ�л����еĻҶ��������
			int nums=count(data);
			//��¼һ�л���һ�������ֵ
			max= nums>max ? nums : max;
			features.put(0, index++, new float[] { nums });
		}
		//���
		if(max>0) {
			for(int i=0; i<features.cols(); i++) {
				float val=(float) (features.get(0, i)[0] / max);
				features.put(0, i, new float[] { val });
			}
		}
		
		
		return features;
	} 
	
	/**
	 * ͳ��һ�л�����һ�лҶ�����Ĵ���
	 * @param mat һ�л���һ�е�����
	 * @return int
	 */
	private static int count(Mat mat) {
		
		int times=0;
		//�����һ��
		if(mat.cols()==1) {
			for(int row=1; row<mat.rows(); row++) {
				//����������еĻҶȷ���������
				if(mat.get(row, 0)[0] != mat.get(row-1, 0)[0]) {
					times++;
				}
			}
		//�����һ��	
		}else if(mat.rows()==1) {
			for(int col=1; col<mat.cols(); col++) {
				if(mat.get(0, col)[0] != mat.get(0, col-1)[0]) {
					times++;
				}
			}
		}
		return times;
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
	 * ʹ��lbp�㷨��ȡ����
	 * @param src ����ͼ��
	 * @param dst ����ͼ��
	 * @param radius �뾶
	 * @param neighbors ���㵱ǰ���LBPֵ����Ҫ���������ص����Ŀ
	 */
	private static void elbp(Mat src, Mat dst, int radius, int neighbors) {
		
		for(int n=0; n<neighbors; n++) {
			float x=(float) (-radius*Math.sin(2.0*n*Math.PI/(float)(neighbors)));
			float y=(float) ( radius*Math.cos(2.0*n*Math.PI/(float)(neighbors)));
			//ȡ��
			int fx=(int) Math.floor(x);
			int fy=(int) Math.floor(y);
			int cx=(int) Math.ceil(x);
			int cy=(int) Math.ceil(y);
			
			//С������
			float ty=y-fy;
			float tx=x-fx;
			
			//���ò�ֵȨ��
			float w1=(1-tx)*(1-ty);
			float w2=tx*(1-ty);
			float w3=(1-tx)*ty;
			float w4=tx*ty;
			
			//ѭ������ͼ������
			for(int i=radius; i<src.rows()-radius; i++) {
				for(int j=radius; j<src.cols()-radius; j++) {
					//�����ֵ
					float t=(float) (w1*src.get(i+fy, j+fx)[0]+w2*src.get(i+fy, j+cx)[0]
							+w3*src.get(i+cy, j+fx)[0]+w4*src.get(i+cy, j+cx)[0]);
					//����
					double a= dst.get(i-radius, j-radius)[0];
					if(t-src.get(i, j)[0] >= 0) {
						a+=(1<<n);
					}else {
						a+=0;
					}
					dst.put(i-radius, j-radius, new float[] { (float) a });
				}
			}
		}
	}
	
	/**
	 * ��������ķ���
	 * @param args
	 */
	public static void main(String []args) {
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.load("C:\\opencv-3.3.0\\build\\java\\x64\\opencv_java330.dll");
		//��������ͼ���λ��
		String file = "E:\\ann\\chinese\\zh_cuan\\chuan_11.jpg";
//		String file="E:\\ImagesFromAndroid\\1513257059606.jpg";
		Mat img=Imgcodecs.imread(file);
		Mat grayImg=new Mat();
		Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_RGB2GRAY);
		gaborFeatures(grayImg);
		
//		Mat features=LBPAndProjectFeatures(img);
//		
//		
//		System.out.println("ȫ�������"+features.cols());
//		for(int i=0; i<features.cols(); i++) {
//			System.out.print(features.get(0, i)[0]+"\t");
//		}
		
//		Imshow.imshow(img);
//		int radius=1;
//		int neighbor=8;
//		Mat dst=new Mat(img.rows(), img.cols(), CvType.CV_32FC1);
//		elbp(img, dst, radius, neighbor);
//		dst.convertTo(dst, CvType.CV_8UC1);
//		Imshow.imshow(dst);
	}
	
	
}
