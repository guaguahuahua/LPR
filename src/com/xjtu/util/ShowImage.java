package com.xjtu.util;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ShowImage {
	
	static JFrame frame;
	/**
	 * �Դ�������ͼƬ������ʾ
	 * @param bfImage
	 */
	public static void showImage(BufferedImage bfImage) {
		//������ʾ����
		frame=new JFrame();
		//���ڵĴ�С���������������Ͻ����꣬��ȣ��߶�
		frame.setBounds(100,100,1024,768);
		//�û������رհ�ťʱ����ִ�еĲ���
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		//���ô����Ƿ�ɼ�����������Ϊtrue��������Ĵ��ڽ�������
		frame.setVisible(true);
		
		//���ñ�ǩ�ı�
		JLabel label=new JLabel("");
		//ͼ��Ĵ�С���������������Ͻ����꣬ͼ��Ŀ�ȣ��߶�
		label.setBounds(0, 0, 1024, 768);
		//���ô��ڵ���ʾ�Ķ���
		frame.getContentPane().add(label);
		label.setIcon(new ImageIcon(bfImage));
	}
	
	/**
	 * ���������أ��������ʾ���������
	 * @param bfImage BufferedImg
	 * @param name String
	 */
	public static void showImage(BufferedImage bfImage, String name) {
		//������ʾ����
		frame=new JFrame();
		//���ڵĴ�С���������������Ͻ����꣬��ȣ��߶�
		frame.setBounds(100,100,1024,768);
		//�û������رհ�ťʱ����ִ�еĲ���
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		//���ô����Ƿ�ɼ�����������Ϊtrue��������Ĵ��ڽ�������
		frame.setVisible(true);
		//���ô�������
		frame.setTitle(name);
		
		//���ñ�ǩ�ı�
		JLabel label=new JLabel("");
		//ͼ��Ĵ�С���������������Ͻ����꣬ͼ��Ŀ�ȣ��߶�
		label.setBounds(0, 0, 1024, 768);
		//���ô��ڵ���ʾ�Ķ���
		frame.getContentPane().add(label);
		label.setIcon(new ImageIcon(bfImage));
	}
}
