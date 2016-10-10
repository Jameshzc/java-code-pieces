package multithreading;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * ���̼߳�ʵ��
 * һ��Ҫ����һ���߳̿��Լ̳�Thread����ʵ��Runnable�ӿڣ���дrun������run�����ǲ���Ҫ�ڳ����е��õģ���Ϊ�����Զ�����
 * �Ƽ�ʵ��Runnable�ӿ�
 * @author hzc
 * @date 2016��9��29������4:30:11
 */
class SeparateSubTask extends Thread {
	private int count = 0;
	private Counter c;
	private boolean runFlag = false;

	public SeparateSubTask(Counter c) {
		this.c = c;
		super.start();
	}

	public void invertFlag() {
		runFlag = !runFlag;
	}

	public void run() {
		while (true) {
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (runFlag) {
				c.t.setText(Integer.toString(count++));
			}
		}
	}
}

public class Counter extends Applet {
	TextField t = new TextField(10);
	private SeparateSubTask sp = null;
	private Button onOff = new Button("Toggle"), start = new Button("Start");

	public void init() {
		add(t);
		start.addActionListener(new StartL());
		add(start);
		onOff.addActionListener(new OnOffL());
		add(onOff);
	}

	class StartL implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (sp == null)
				sp = new SeparateSubTask(Counter.this);
		}

	}

	class OnOffL implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (sp != null)
				sp.invertFlag();
		}
	}

	public static void main(String[] args) {
		Counter applet = new Counter();
		Frame aFrame = new Frame("Counter");
		aFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		aFrame.add(applet, BorderLayout.CENTER);
		aFrame.setSize(300,200);
		applet.init();
		applet.start();
		aFrame.setVisible(true);
	}

}
