package com.xjtu.preprocess;

import org.opencv.core.Rect;

/**
 * ��������ҪĿ����Ϊ�˽����MSER��map��Ϊ��ֵ��������Զ������˲�������
 * @author Administrator
 *
 */
public class ScoredRect {
	private int index;
	private double score;
	private Rect rect;
	
	public ScoredRect(int index, double score, Rect rect) {
		this.index=index;
		this.score=score;
		this.rect=rect;
	}
	
	
	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


	public double getScore() {
		return score;
	}


	public void setScore(double score) {
		this.score = score;
	}


	public Rect getRect() {
		return rect;
	}


	public void setRect(Rect rect) {
		this.rect = rect;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
