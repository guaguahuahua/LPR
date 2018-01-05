package com.xjtu.charseperate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.DrawBoundingRectangle;
import com.xjtu.util.Imshow;

public class SeperateChars {

	
	/**
	 * �����ַ����з֣����ȶ�ֵ����Ȼ��ʹ���������ķ�ʽ�����з�
	 * @param plateImg ���Ƶ�ͼ��
	 * @param charsImg �зֿ�֮�������ַ��ļ���
	 */
	public static int seperateChars(Mat plateImg, List<Mat> charsImg) {
		Imshow.imshow(plateImg);
		//�ҶȻ�
		Mat grayImg=new Mat();
		Imgproc.cvtColor(plateImg, grayImg, Imgproc.COLOR_BGR2GRAY);
//		Imshow.imshow(grayImg);
		//��ֵ��
		Mat binaryImg=new Mat();
		//�Ҷ�ͼ����ֵͼ����ֵͼ�Ľϴ�ֵ��ʹ��ʲô���ľֲ�����Ӧ����
		Imgproc.adaptiveThreshold(grayImg, binaryImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -20);//25,10��25��0
		Imshow.imshow(binaryImg);
		Mat noRivetImg=cleanRivet(binaryImg);
		//��Ҫ��Ϊ�˷�ֹ��������Ĺ����з�����δ��λ�����Ƶ�ͼ��
		if(noRivetImg!=null) {
			Imshow.imshow(noRivetImg);
		}else {
			System.out.println("SeperateChars.java:"+"���í���Ĺ��̷��ֲ��ǳ���ͼ��");
			return -1;
		}
		//��λ�����ַ��������зֳ��������ַ�
		findSpecificContour(noRivetImg, charsImg);
		return 1;
	}
	
	/**
	 * �������
	 * @param binaryMat
	 * @return
	 */
	public static Mat cleanRivet(Mat binaryMat) {
		//��¼ÿһ�лҶ�����Ĵ���
		int count;
		//���ÿһ������Ĵ���
		List<Integer> times=new ArrayList<Integer>();
		//��¼���еİ�ɫ���ص�
		int white=0;
		for(int row=0; row<binaryMat.rows(); row++) {
			count=0;
			for(int col=0; col<binaryMat.cols()-1; col++) {
				//һ�������������ز���ȣ���Ϊ������һ������
				if(binaryMat.get(row, col)[0]!=binaryMat.get(row, col+1)[0]) {
					count++;
				}
				//�����Ϊ������ͳ�ƴ���+1
				if(binaryMat.get(row, col)[0]==255) {
					white++;
				}
			}
			//����row�е�����Ĵ�����ŵ���̬������
			times.add(count);
		}
		//�鿴ÿһ�лҶ�����Ĵ��������������ֵ�������������0.6����ô��Ϊ����ͼ��λʧ����
		int jumps=0;
		for(int i=0; i<times.size(); i++) {
			if(times.get(i)>=16 && times.get(i)<=45) {
				jumps++;
			}
		}
		if(jumps*1.0/times.size()*1.0<0.4) {
			System.out.println("SeperateChars.java:"+"��������������������λ���������ǳ��ƣ�����������");
			return null;
		}
		//�鿴��ɫ�����������ռ��
		if(white*1.0/(binaryMat.rows()*binaryMat.cols())<0.15 || white*1.0/(binaryMat.rows()*binaryMat.cols())>0.5) {
			System.out.println("SperateChars.java:"+"�����������ռ�Ȳ�������:��λ���������ǳ��ƣ�����������");
			return null;
		}
		//���������ڵ�����Ϊ����������Ӱ��
		for(int i=0; i<times.size(); i++) {
			int t=times.get(i);
			//����������<16 ��ô�ͽ�������Ϊ����
			if(t<16) {
				for(int col=0; col<binaryMat.cols(); col++) {
					binaryMat.put(i, col, new byte[] {0});
				}
			}
		}
		return binaryMat;
	}
	
	/**
	 * ��λ�����ַ��������зֳ��������ַ�
	 * @param noRivetMat
	 */
	public static void findSpecificContour(Mat noRivetMat, List<Mat>chars) {
		//��¼�ַ������б߿������Ŀ�͸�
		int width=0;
		int height=0;
		//������Ҫ��Ϊ�˴�Ų��ҵ���������׼��
		List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
		Mat hierarchy=new Mat();
		//��������
		Imgproc.findContours(noRivetMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		//������е���Ӿ���
		List<Rect> rectangles=new ArrayList<Rect>();
		//������������Ĵ�ֱ��Ӿ���		
		for(int i=0; i<contours.size(); i++) {
			//��ȡһ�������Ĵ�ֱ������
			Rect rect=Imgproc.boundingRect(contours.get(i));
			//����ǻ���ַ����������Ŀ�͸ߣ�Ȼ����������Ϊ�ü��ַ��ı�׼��͸�
			if(rect.width>width) {
				width=rect.width;
			}
			if(rect.height>height) {
				height=rect.height;
			}
			rectangles.add(rect);			
		}
		//����ҵ���ֱ�������������
		System.out.println("��Ӿ��εļ��ϴ�СΪ��"+rectangles.size());
		//�����ַ����������
//		DrawBoundingRectangle.drawBoundingRectangle_1(noRivetMat, rectangles);
		//չʾ��������Ӿ��εĳ���ͼ��
		Imshow.imshow(noRivetMat);
		//�����Ѿ��ҵ������е���Ӿ��Σ�����Ҫ���ľ����ҵ������ַ�ǰ����Ǹ�Ӣ���ַ����Ӷ��õ������ַ������ҽ�������ڸ�Ӣ���ַ���
		//�ҵ������ַ���λ��
		List<Integer> index=findSpecificChar(noRivetMat, rectangles, width, height);
		if(index.get(0)<19) {
			System.out.println("SeperateChars:ͼ�����̫�࣬����������");
			return;
		}
		System.out.println("����λ�õ��ַ����꣺"+index);
		//����зֳ�����7���ַ�
//		List<Mat> chars=new ArrayList<Mat>();
		//���������ַ���λ��
		int x=(int) (index.get(0)-1.15*width);
		//����Ϊ�˷�ֹ���͵���㣬���͵�������ɸ�ֵ  13���ַ��Ŀ��
		boolean flag=false;
		if(x<0) {
			x=(int) (index.get(0)-1.15*14);
			//һ�����ܶ࣬����ҪʱĬ��ֵ��������ʹ��������Ӿ���ȥ�ü�����������Խ��
			flag=true; 
		}
		//�ü������ַ�
		Rect cut=new Rect(x, index.get(1), width, height);
		//���࣬ʹ��Ĭ��ֵ����ֹԽ��
		if(flag) {
			cut=new Rect(x, index.get(1), 14, 21);
		}
		Mat chinese=new Mat(noRivetMat, cut);
		chars.add(chinese);
		//�ü������ַ�
		Rect sCut=new Rect(index.get(0), index.get(1), width, height);
		//���࣬ʹ��Ĭ��ֵ����ֹԽ��
		if(flag) {
			sCut=new Rect(index.get(0), index.get(1), 14, 21);
		}
		Mat special=new Mat(noRivetMat, sCut);
		chars.add(special);
		//����������x�����򣬷������ɸ���ַ�,���ܴ�Ŀռ���Ҫ��Ϊ�˷�ֹ��������Ӱ��
		List<Integer>xIndex=new ArrayList<Integer>();
		for(int i=0; i<rectangles.size(); i++) {
			Rect t=rectangles.get(i);
			//ֻ��x������������ַ���������������
			if(t.x>index.get(0)) {
				xIndex.add(t.x);
			}
		}
		//����̬����ת��Ϊ�����������
		Object []charIndex=xIndex.toArray();
		Arrays.sort(charIndex);
		//Ϊ�˷�ֹ�����ַ�������ͬʱ���Ὣͳһ���ַ������Σ�������ĳλ�ü��н���֮����º���ȶԵ�x�����ֵ
		int renewIndex=index.get(0);
		//����������������������������ַ���5���ַ��зֳ���
		for(int k=0, i=0; k<charIndex.length && i<5; k++) {
			for(int j=0; j<rectangles.size(); j++) {
				//����ȡ��һ������
				Rect r=rectangles.get(j);
				//�ж���������Ƿ���������ε��Ҳ࣬���Ҳ������
				if((int)charIndex[k]==r.x && r.x>renewIndex && (r.height>0.8*height)) {
					//�ü��ַ�����Ӧ�ð����ַ�����Ӿ��εĴ�С���вü������������ı߿���ܳ���Խ���������������п��ܲõ������ַ�
					//�ü�������
					Mat oridinary=new Mat(noRivetMat, r);
//					Imshow.imshow(oridinary);
					chars.add(oridinary);
					//�����ַ�����++����5������֮ǰ�Ѿ��ü������ĺ������ַ�
					i++;
					renewIndex=r.x;
				}
			}
		}
//		for(Mat K: chars) {
//			Imshow.imshow(K);
//		}
	}
	/**
	 * Ѱ��λ������ͼ��1/7--2/7֮����ַ�
	 * @param noRivetMat ���í��֮��Ķ�ֵͼ��
	 * @param rectangles �����ַ�����Ӿ�������
	 * @param maxWidth   ��Ӿ��ε�����
	 * @param maxHeight  ��Ӿ��ε�����
	 * @return
	 */
	public static List<Integer> findSpecificChar(Mat noRivetMat, List<Rect> rectangles, int maxWidth, int maxHeight) {		
		//��¼�����ַ���λ�ã�0Ϊx���꣬1Ϊy����
		List<Integer> position=new ArrayList<Integer>();
		//�������߿��0.8��͸ߣ���Ҫ��Ϊ�˷�ֹ��������
		double width=(double) (0.8*maxWidth);
		double height=(double) (0.8*maxHeight);
		//���x��λ��λ������ͼ���1/7��2/7֮�䣬��ô��Ϊ��������������ַ��Ҳ�ľ���
		double se1=(double) (noRivetMat.cols())/7;
		double se2=(double) (noRivetMat.cols()*2)/7;
		System.out.println(se1+", "+se2);
		//�������е��������ҵ�һ����������Ӿ��ε�x����������ͼ���1/7--2/7֮��
		for(int i=0; i<rectangles.size(); i++) {
			//����ȡ��һ����Ӵ�ֱ����
			Rect rect=rectangles.get(i);
			//��������ε�����x����λ��
			int x=(int) (rect.x+(double) (rect.width*0.5));
			if((rect.width> width || rect.height>height) && (x>se1 && x<se2)) {
				if(position.isEmpty()) {
					position.add(rect.x);
					position.add(rect.y);
				}else {
					if(position.get(0)<rect.x) {
						position.clear();
						position.add(rect.x);
						position.add(rect.y);
					}
				}
//				//һ���ҵ���һ��λ�ã�����Ͳ����ٴβ��ң���������
//				break;
			}
		}
		return position;
	} 
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
